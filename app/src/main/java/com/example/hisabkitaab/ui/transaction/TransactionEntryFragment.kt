package com.example.hisabkitaab.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

class TransactionEntryFragment : Fragment() {
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(KitaabDatabase(requireContext()).getTransactionDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_transaction_entry, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transactionId = arguments?.getInt("arg_transaction_id") ?: return

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarTransactionEntry)
        val amountEt = view.findViewById<EditText>(R.id.etEntryAmount)
        val noteEt = view.findViewById<EditText>(R.id.etEntryNote)
        val dateTv = view.findViewById<TextView>(R.id.tvEntryDate)
        val saveButton = view.findViewById<Button>(R.id.btnSaveEntry)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        lifecycleScope.launch {
            val tx = withContext(Dispatchers.IO) { viewModel.getTransactionById(transactionId) } ?: return@launch
            val isDiye = viewModel.isDiyeType(tx.type)
            toolbar.title = if (isDiye) "Maine diye" else "Maine liye"
            saveButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(if (isDiye) "#FF4B5F" else "#10B760")
            )
            amountEt.setText(tx.amount.toInt().toString())
            noteEt.setText(tx.note ?: "")
            dateTv.text = SimpleDateFormat("dd MMM, yy", Locale.getDefault()).format(Date(tx.date))

            saveButton.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.updateTransaction(
                            tx.copy(
                                amount = amountEt.text.toString().toDoubleOrNull() ?: tx.amount,
                                note = noteEt.text.toString()
                            )
                        )
                    }
                    Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }
}
