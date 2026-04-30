package com.example.hisabkitaab.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.repository.TransactionRepository
import com.example.hisabkitaab.data.room.KitaabDatabase
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionDetailFragment : Fragment() {
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(KitaabDatabase(requireContext()).getTransactionDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_transaction_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transactionId = arguments?.getInt("arg_transaction_id") ?: return
        val customerId = arguments?.getInt("arg_customer_id") ?: 0

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarTransactionDetail)
        val amountEt = view.findViewById<TextView>(R.id.etDetailAmount)
        val noteEt = view.findViewById<TextView>(R.id.etDetailNote)
        val dateTv = view.findViewById<TextView>(R.id.tvDetailDate)
        val editButton = view.findViewById<Button>(R.id.btnEditTransaction)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        lifecycleScope.launch {
            val tx = withContext(Dispatchers.IO) { viewModel.getTransactionById(transactionId) } ?: return@launch
            val isDiye = viewModel.isDiyeType(tx.type)
            toolbar.title = if (isDiye) "Maine diye" else "Maine liye"
            amountEt.text = "Rs. ${tx.amount.toInt()}"
            noteEt.text = tx.note?.ifBlank { "Tafseel (optional)" } ?: "Tafseel (optional)"
            dateTv.text = SimpleDateFormat("dd MMM, yy", Locale.getDefault()).format(Date(tx.date))

            editButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_transactionDetailFragment_to_transactionEntryFragment,
                    bundleOf(
                        "arg_transaction_id" to tx.id,
                        "arg_customer_id" to customerId
                    )
                )
            }
        }
    }
}
