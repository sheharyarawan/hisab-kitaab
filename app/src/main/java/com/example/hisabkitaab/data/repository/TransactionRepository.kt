package com.example.hisabkitaab.data.repository

import androidx.room.Transaction
import com.example.hisabkitaab.data.room.TransactionDao

class TransactionRepository(
    private val transactionDao: TransactionDao
) {

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getTransactions(customerId: Int): List<Transaction> {
        return transactionDao.getTransactions(customerId)
    }

    suspend fun deleteCustomerTransactions(customerId: Int) {
        transactionDao.deleteCustomerTransactions(customerId)
    }
}