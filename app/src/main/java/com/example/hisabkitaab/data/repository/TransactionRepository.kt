package com.example.hisabkitaab.data.repository

import com.example.hisabkitaab.data.entity.Transaction
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

    suspend fun getTransactionById(transactionId: Int): Transaction? {
        return transactionDao.getTransactionById(transactionId)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteCustomerTransactions(customerId: Int) {
        transactionDao.deleteCustomerTransactions(customerId)
    }
}