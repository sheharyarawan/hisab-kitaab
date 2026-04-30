package com.example.hisabkitaab.ui.customer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
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

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CustomerAdapter { item ->
            findNavController().navigate(
                R.id.action_customerFragment_to_customerDetailFragment,
                bundleOf("arg_customer_id" to item.customerId)
            )
        }
        recyclerView.adapter = adapter

        // Keeping add button inactive for now as requested.
        addButton.setOnClickListener { }

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
        val totalDebitView = root.findViewById<TextView>(R.id.tvTotalDebit)
        val totalCreditView = root.findViewById<TextView>(R.id.tvTotalCredit)
        lifecycleScope.launch {
            loadCustomerRows(totalDebitView, totalCreditView)
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
}