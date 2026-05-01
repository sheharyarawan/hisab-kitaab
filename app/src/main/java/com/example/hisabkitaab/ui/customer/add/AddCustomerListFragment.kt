package com.example.hisabkitaab.ui.customer.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.entity.Customer
import com.example.hisabkitaab.data.model.ContactUiModel
import com.example.hisabkitaab.data.repository.CustomerRepository
import com.example.hisabkitaab.data.room.KitaabDatabase
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCustomerListFragment : Fragment() {
    private lateinit var repository: CustomerRepository
    private lateinit var adapter: AddCustomerAdapter
    private var allContacts: List<ContactUiModel> = emptyList()

    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadContacts()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = CustomerRepository(KitaabDatabase(requireContext()).getCustomerDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_customer_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarAddCustomer)
        val searchEt = view.findViewById<EditText>(R.id.etSearchContact)
        val clearIv = view.findViewById<ImageView>(R.id.ivClearSearch)
        val manualLayout = view.findViewById<LinearLayout>(R.id.layoutCreateManual)
        val contactsRv = view.findViewById<RecyclerView>(R.id.recyclerViewContacts)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        toolbar.title= "Add Customer"

        adapter = AddCustomerAdapter { contact -> addContactAsCustomer(contact) }
        contactsRv.layoutManager = LinearLayoutManager(requireContext())
        contactsRv.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        contactsRv.addItemDecoration(divider)

        searchEt.doAfterTextChanged { applySearch(it?.toString().orEmpty()) }
        clearIv.setOnClickListener { searchEt.text?.clear() }
        manualLayout.setOnClickListener {
            findNavController().navigate(R.id.action_addCustomerListFragment_to_addCustomerManualFragment)
        }

        ensurePermissionAndLoad()
    }

    override fun onResume() {
        super.onResume()
        ensurePermissionAndLoad()
    }

    private fun ensurePermissionAndLoad() {
        val isGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            loadContacts()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun showPermissionDeniedDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Contacts permission needed")
            .setMessage("Allow contacts permission to show your contact list.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .create()
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(android.graphics.Color.RED)
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            val existingPhones = withContext(Dispatchers.IO) {
                repository.getAllCustomers().map { normalizePhone(it.phone) }.toSet()
            }

            val contacts = withContext(Dispatchers.IO) {
                val map = linkedMapOf<String, ContactUiModel>()
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val cursor = requireContext().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex)?.trim().orEmpty().ifBlank { "Unknown" }
                        val phoneRaw = it.getString(phoneIndex)?.trim().orEmpty()
                        val normalized = normalizePhone(phoneRaw)
                        if (normalized.isBlank()) continue
                        if (!map.containsKey(normalized)) {
                            map[normalized] = ContactUiModel(
                                displayName = name,
                                phone = phoneRaw,
                                initials = buildInitials(name),
                                isAdded = existingPhones.contains(normalized)
                            )
                        }
                    }
                }
                map.values.toList()
            }

            allContacts = contacts
            adapter.submitList(allContacts)
        }
    }

    private fun addContactAsCustomer(contact: ContactUiModel) {
        lifecycleScope.launch {
            val newId = withContext(Dispatchers.IO) {
                repository.insertCustomer(Customer(name = contact.displayName, phone = contact.phone))
            }.toInt()

            findNavController().navigate(
                R.id.action_addCustomerListFragment_to_customerDetailFragment,
                bundleOf("arg_customer_id" to newId)
            )
        }
    }

    private fun applySearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            adapter.submitList(allContacts)
            return
        }
        adapter.submitList(
            allContacts.filter {
                it.displayName.contains(q, ignoreCase = true) || it.phone.contains(q, ignoreCase = true)
            }
        )
    }

    private fun buildInitials(name: String): String {
        return name.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "?" }
    }

    private fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }
}
