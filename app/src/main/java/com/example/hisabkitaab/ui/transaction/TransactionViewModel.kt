package com.example.hisabkitaab.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabkitaab.data.entity.Transaction
import com.example.hisabkitaab.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    suspend fun getTransactions(customerId: Int): List<Transaction> {
        return repository.getTransactions(customerId)
    }

    suspend fun getTransactionById(transactionId: Int): Transaction? {
        return repository.getTransactionById(transactionId)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        repository.updateTransaction(transaction)
    }

    fun isDiyeType(type: String): Boolean {
        return type.equals("add", true) ||
            type.equals("debit", true) ||
            type.equals("lene", true)
    }
}