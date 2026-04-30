package com.example.hisabkitaab.data.model

data class CustomerTransactionUiModel(
    val transactionId: Int,
    val transactionType: String,
    val amount: Double,
    val dateMillis: Long,
    val dateText: String,
    val noteText: String,
    val balanceText: String,
    val diyeText: String,
    val liyeText: String
)
