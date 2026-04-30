package com.example.hisabkitaab.data.model

data class CustomerBalance(
    val customerId: Int,
    val name: String,
    val phone: String,
    val totalDebit: Double,
    val totalCredit: Double,
    val netAmount: Double
)
