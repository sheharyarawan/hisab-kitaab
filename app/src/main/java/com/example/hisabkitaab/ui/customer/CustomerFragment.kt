package com.example.hisabkitaab.ui.customer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.entity.UserProfile
import com.example.hisabkitaab.data.model.CustomerBalance
import com.example.hisabkitaab.data.room.KitaabDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class CustomerFragment : Fragment() {
    private lateinit var adapter: CustomerAdapter
    private var allItems: List<CustomerBalance> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_customer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalDebitView = view.findViewById<TextView>(R.id.tvTotalDebit)
        val totalCreditView = view.findViewById<TextView>(R.id.tvTotalCredit)
        val searchView = view.findViewById<EditText>(R.id.etSearchCustomer)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCustomers)
        val addButton = view.findViewById<Button>(R.id.btnAddCustomer)
        val db = KitaabDatabase(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarCustomer)
        toolbar.setOnClickListener {
            showProfileDialog()
        }

        adapter = CustomerAdapter(
            onItemClick = { item ->
                findNavController().navigate(
                    R.id.action_customerFragment_to_customerDetailFragment,
                    bundleOf("arg_customer_id" to item.customerId)
                )
            },
            onItemLongClick = { item ->
                showDeleteDialog(
                    message = "Delete ${item.name} and all entries?",
                    onConfirm = {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                db.getTransactionDao().deleteCustomerTransactions(item.customerId)
                                db.getCustomerDao().getCustomerById(item.customerId).let { db.getCustomerDao().deleteCustomer(it) }
                            }
                            loadCustomerRows(totalDebitView, totalCreditView)
                        }
                    }
                )
            }
        )
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_customerFragment_to_addCustomerListFragment)
        }

        searchView.doAfterTextChanged {
            applyFilter(it?.toString().orEmpty(), totalDebitView, totalCreditView)
        }

        lifecycleScope.launch {
            loadCustomerRows(totalDebitView, totalCreditView)
        }
    }
    override fun onResume() {
        super.onResume()

        val root = view ?: return
        val debit = root.findViewById<TextView>(R.id.tvTotalDebit)
        val credit = root.findViewById<TextView>(R.id.tvTotalCredit)

        lifecycleScope.launch {
            loadCustomerRows(debit, credit)
            updateToolbarName()
        }
    }

    private suspend fun loadCustomerRows(totalDebitView: TextView, totalCreditView: TextView) {
        allItems = withContext(Dispatchers.IO) {
            val db = KitaabDatabase(requireContext())
            val customers = db.getCustomerDao().getAllCustomers()
            val txDao = db.getTransactionDao()

            customers.map { customer ->
                val tx = txDao.getTransactions(customer.id)
                val debit = tx.filter {
                    it.type.equals("add", true) ||
                        it.type.equals("debit", true) ||
                        it.type.equals("lene", true)
                }.sumOf { it.amount }
                val credit = tx.filter {
                    it.type.equals("del", true) ||
                        it.type.equals("credit", true) ||
                        it.type.equals("dene", true)
                }.sumOf { it.amount }

                CustomerBalance(
                    customerId = customer.id,
                    name = customer.name,
                    phone = customer.phone,
                    totalDebit = debit,
                    totalCredit = credit,
                    netAmount = credit - debit
                )
            }
        }

        applyFilter("", totalDebitView, totalCreditView)
    }

    private fun applyFilter(query: String, totalDebitView: TextView, totalCreditView: TextView) {
        val q = query.trim()
        val filtered = if (q.isBlank()) {
            allItems
        } else {
            allItems.filter {
                it.name.contains(q, ignoreCase = true) || it.phone.contains(q, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        updateTopCards(filtered, totalDebitView, totalCreditView)
    }

    private fun updateTopCards(items: List<CustomerBalance>, totalDebitView: TextView, totalCreditView: TextView) {
        val redTotal = items.filter { it.netAmount < 0 }.sumOf { abs(it.netAmount) }
        val greenTotal = items.filter { it.netAmount > 0 }.sumOf { it.netAmount }

        totalDebitView.text = "Rs. ${redTotal.toInt()}"
        totalCreditView.text = "Rs. ${greenTotal.toInt()}"
    }

    private fun showDeleteDialog(message: String, onConfirm: () -> Unit) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage(message)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> onConfirm() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.RED)
    }
    private fun saveProfileName(name: String) {

        val db = KitaabDatabase(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {

            db.profileDao().insertProfile(
                UserProfile(id = 1, name = name)
            )

            withContext(Dispatchers.Main) {
                updateToolbarName()
            }
        }
    }

    private fun updateToolbarName() {

        val db = KitaabDatabase(requireContext())
        val toolbar = view?.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarCustomer)

        lifecycleScope.launch(Dispatchers.IO) {

            val profile = db.profileDao().getProfile()

            withContext(Dispatchers.Main) {

                toolbar?.title = "${profile?.name}   ⌄" ?: "Your Name   ⌄"
            }
        }
    }
    private fun showProfileDialog() {

        val input = EditText(requireContext())
        input.hint = "Enter your name"

        val db = KitaabDatabase(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {
            val existing = db.profileDao().getProfile()

            withContext(Dispatchers.Main) {

                input.setText(existing?.name ?: "")

                AlertDialog.Builder(requireContext())
                    .setTitle("Profile Name")
                    .setView(input)
                    .setPositiveButton("Save") { dialog, _ ->

                        val name = input.text.toString()
                        saveProfileName(name)

                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}