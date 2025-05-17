package com.example.emperorchess.models

data class SaleItem(
    val inventoryItemId: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val total: Double
) 