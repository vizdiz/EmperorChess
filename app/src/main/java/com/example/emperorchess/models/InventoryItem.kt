package com.example.emperorchess.models

data class InventoryItem(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val description: String = "",
    val price: Double,
    val stock: Int,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)