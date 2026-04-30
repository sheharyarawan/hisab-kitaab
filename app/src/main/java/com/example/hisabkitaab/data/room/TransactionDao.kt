package com.example.hisabkitaab.data.room

import androidx.room.*
import com.example.hisabkitaab.data.entity.Transaction

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    suspend fun getTransactions(customerId: Int): List<Transaction>

    @Query("DELETE FROM transactions WHERE customerId = :customerId")
    suspend fun deleteCustomerTransactions(customerId: Int)
}