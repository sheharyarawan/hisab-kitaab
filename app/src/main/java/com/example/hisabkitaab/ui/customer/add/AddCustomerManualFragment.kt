package com.example.hisabkitaab.ui.customer.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.entity.Customer
import com.example.hisabkitaab.data.repository.CustomerRepository
import com.example.hisabkitaab.data.room.KitaabDatabase
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCustomerManualFragment : Fragment() {
    private lateinit var repository: CustomerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = CustomerRepository(KitaabDatabase(requireContext()).getCustomerDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_customer_manual, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarAddCustomerManual)
        val nameEt = view.findViewById<EditText>(R.id.etManualName)
        val phoneEt = view.findViewById<EditText>(R.id.etManualPhone)
        val addButton = view.findViewById<Button>(R.id.btnManualAdd)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        toolbar.title= "Add Customer"

        addButton.setOnClickListener {
            val name = nameEt.text?.toString()?.trim().orEmpty()
            val phone = phoneEt.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.isBlank()) {
                Toast.makeText(requireContext(), "Enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val newId = withContext(Dispatchers.IO) {
                    repository.insertCustomer(Customer(name = name, phone = phone))
                }.toInt()
                findNavController().navigate(
                    R.id.action_addCustomerManualFragment_to_customerDetailFragment,
                    bundleOf("arg_customer_id" to newId)
                )
            }
        }
    }
}
