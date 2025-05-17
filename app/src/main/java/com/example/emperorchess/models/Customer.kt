package com.example.emperorchess.models

data class Customer(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val address: String = "",
    val notes: String = ""
) 