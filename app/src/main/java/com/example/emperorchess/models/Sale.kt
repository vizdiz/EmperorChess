package com.example.emperorchess.models

import java.text.SimpleDateFormat
import java.util.*

data class Sale(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val customerName: String,
    val date: String,  // Format: "yyyy-MM-dd HH:mm:ss"
    val items: List<SaleItem>,
    var totalAmount: Double = items.sumOf { it.total },
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val DISPLAY_FORMAT = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
        val GRAPH_FORMAT = SimpleDateFormat("MMM dd", Locale.getDefault())

        fun createWithCurrentDate(
            customerId: String,
            customerName: String,
            items: List<SaleItem>,
            totalAmount: Double,
            notes: String = "",
            paymentMethod: String = "Cash"
        ): Sale {
            return Sale(
                customerId = customerId,
                customerName = customerName,
                date = DATE_FORMAT.format(Date()),
                items = items,
                totalAmount = totalAmount,
                notes = notes,
                paymentMethod = paymentMethod
            )
        }

        fun parseDate(dateStr: String): Date? {
            return try {
                DATE_FORMAT.parse(dateStr)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getDisplayDate(): String {
        return try {
            val dateObj = DATE_FORMAT.parse(date)
            dateObj?.let { DISPLAY_FORMAT.format(it) } ?: date
        } catch (e: Exception) {
            date
        }
    }

    fun getGraphDate(): String {
        return try {
            val dateObj = DATE_FORMAT.parse(date)
            dateObj?.let { GRAPH_FORMAT.format(it) } ?: date
        } catch (e: Exception) {
            date
        }
    }
} 