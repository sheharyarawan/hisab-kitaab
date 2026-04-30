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
}