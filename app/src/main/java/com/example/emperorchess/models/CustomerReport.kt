package com.example.emperorchess.models

data class CustomerReport(
    val customerId: String,
    val customerName: String,
    val totalPurchases: Double = 0.0,
    val lastPurchaseDate: String = "",
    val numberOfPurchases: Int = 0,
    val totalAmount: Double = 0.0
) 