package com.example.hisabkitaab.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.repository.CustomerRepository
import com.example.hisabkitaab.data.repository.TransactionRepository
import com.example.hisabkitaab.data.room.KitaabDatabase
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class CustomerDetailFragment : Fragment() {
    private val transactionAdapter = CustomerTransactionAdapter { item ->
        findNavController().navigate(
            R.id.action_customerDetailFragment_to_transactionDetailFragment,
            bundleOf(
                "arg_transaction_id" to item.transactionId,
                "arg_customer_id" to (arguments?.getInt(ARG_CUSTOMER_ID) ?: 0)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_customer_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customerId = arguments?.getInt(ARG_CUSTOMER_ID) ?: return

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarCustomerDetail)
        val summaryAmountView = view.findViewById<TextView>(R.id.tvSummaryAmount)
        val summaryLabelView = view.findViewById<TextView>(R.id.tvSummaryLabel)
        val transactionsRv = view.findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        val diyeButton = view.findViewById<Button>(R.id.btnMaineDiye)
        val liyeButton = view.findViewById<Button>(R.id.btnMaineLiye)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        transactionsRv.layoutManager = LinearLayoutManager(requireContext())
        transactionsRv.adapter = transactionAdapter

        diyeButton.setOnClickListener {
            Toast.makeText(requireContext(), "Maine Diye screen next", Toast.LENGTH_SHORT).show()
        }
        liyeButton.setOnClickListener {
            Toast.makeText(requireContext(), "Maine Liye screen next", Toast.LENGTH_SHORT).show()
        }

        val db = KitaabDatabase(requireContext())
        val customerRepo = CustomerRepository(db.getCustomerDao())
        val txRepo = TransactionRepository(db.getTransactionDao())

        lifecycleScope.launch {
            val customer = withContext(Dispatchers.IO) { customerRepo.getCustomerById(customerId) }
            val transactions = withContext(Dispatchers.IO) { txRepo.getTransactions(customerId) }

            val debitTotal = transactions.filter {
                it.type.equals("add", true) || it.type.equals("debit", true) || it.type.equals("lene", true)
            }.sumOf { it.amount }
            val creditTotal = transactions.filter {
                it.type.equals("del", true) || it.type.equals("credit", true) || it.type.equals("dene", true)
            }.sumOf { it.amount }
            val net = creditTotal - debitTotal

            toolbar.title = customer.name
            summaryAmountView.text = "Rs. ${abs(net).toInt()}"
            summaryLabelView.text = if (net >= 0) "Maine dene hain" else "Maine lene hain"

            val oldestToNewest = transactions.sortedBy { it.date }
            var runningBalance = 0.0
            val runningBalanceById = mutableMapOf<Int, Double>()
            oldestToNewest.forEach { tx ->
                if (tx.type.equals("add", true) || tx.type.equals("debit", true) || tx.type.equals("lene", true)) {
                    runningBalance -= tx.amount
                } else if (tx.type.equals("del", true) || tx.type.equals("credit", true) || tx.type.equals("dene", true)) {
                    runningBalance += tx.amount
                }
                runningBalanceById[tx.id] = runningBalance
            }

            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            val rows = transactions.map { tx ->
                val isDiye = tx.type.equals("add", true) || tx.type.equals("debit", true) || tx.type.equals("lene", true)
                val isLiye = tx.type.equals("del", true) || tx.type.equals("credit", true) || tx.type.equals("dene", true)
                CustomerTransactionUi(
                    transactionId = tx.id,
                    transactionType = tx.type,
                    amount = tx.amount,
                    dateMillis = tx.date,
                    dateText = formatter.format(Date(tx.date)),
                    noteText = tx.note?.ifBlank { "-" } ?: "-",
                    balanceText = "Bal. Rs. ${(runningBalanceById[tx.id] ?: 0.0).toInt()}",
                    diyeText = if (isDiye) "Rs. ${tx.amount.toInt()}" else "",
                    liyeText = if (isLiye) "Rs. ${tx.amount.toInt()}" else ""
                )
            }
            transactionAdapter.submitList(rows)
        }
    }

    companion object {
        private const val ARG_CUSTOMER_ID = "arg_customer_id"
    }
}