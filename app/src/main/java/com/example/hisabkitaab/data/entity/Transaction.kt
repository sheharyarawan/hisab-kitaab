package com.example.hisabkitaab.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val customerId: Int,

    val amount: Double,

    val type: String,

    val note: String? = null,

    val date: Long = System.currentTimeMillis()
)
