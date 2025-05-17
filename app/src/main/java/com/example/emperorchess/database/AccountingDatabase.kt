package com.example.emperorchess.database

import android.content.Context
import android.content.SharedPreferences
import com.example.emperorchess.models.Customer
import com.example.emperorchess.models.CustomerReport
import com.example.emperorchess.models.InventoryItem
import com.example.emperorchess.models.Sale
import com.example.emperorchess.models.SaleItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import com.google.gson.Gson

class AccountingDatabase(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("accounting_data", Context.MODE_PRIVATE)

    // Inventory methods
    fun saveInventoryItem(item: InventoryItem) {
        val items = getInventoryItems().toMutableList()
        val index = items.indexOfFirst { it.id == item.id }
        
        if (index >= 0) {
            items[index] = item.copy()
        } else {
            items.add(item)
        }
        
        saveInventoryItems(items)
    }
    
    fun getInventoryItems(): List<InventoryItem> {
        val jsonArray = sharedPreferences.getString("inventory_items", "[]") ?: "[]"
        val items = mutableListOf<InventoryItem>()
        
        try {
            val array = JSONArray(jsonArray)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                items.add(
                    InventoryItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        price = obj.getDouble("price"),
                        stock = obj.getInt("stock"),
                        category = obj.optString("category", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return items
    }

    private fun saveInventoryItems(items: List<InventoryItem>) {
        val jsonArray = JSONArray()
        
        for (item in items) {
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("description", item.description)
                put("price", item.price)
                put("stock", item.stock)
                put("category", item.category)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            jsonArray.put(jsonObject)
        }
        
        sharedPreferences.edit().putString("inventory_items", jsonArray.toString()).apply()
    }

    // Sales methods
    fun saveSale(sale: Sale) {
        // First check if we have enough stock for all items
        val inventoryItems = getInventoryItems().toMutableList()
        
        // Verify stock for all items first
        sale.items.forEach { saleItem ->
            val inventoryItem = inventoryItems.find { it.id == saleItem.inventoryItemId }
                ?: throw Exception("Inventory item not found")
            
            if (inventoryItem.stock < saleItem.quantity) {
                throw Exception("Not enough stock for ${inventoryItem.name}")
            }
        }

        // If we have enough stock, proceed with the updates
        sale.items.forEach { saleItem ->
            val inventoryItem = inventoryItems.find { it.id == saleItem.inventoryItemId }!!
            val updatedItem = inventoryItem.copy(
                stock = inventoryItem.stock - saleItem.quantity,
                updatedAt = System.currentTimeMillis()
            )
            saveInventoryItem(updatedItem)
        }

        // Save the sale
        val sales = getSales().toMutableList()
        sales.add(sale)
        val json = Gson().toJson(sales)
        sharedPreferences.edit()
            .putString("sales", json)
            .apply()

        // Update customer report
        updateCustomerReport(sale)
    }
    
    fun getSales(): List<Sale> {
        val jsonArray = sharedPreferences.getString("sales", "[]") ?: "[]"
        val sales = mutableListOf<Sale>()
        
        try {
            val array = JSONArray(jsonArray)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val itemsArray = obj.getJSONArray("items")
                val saleItems = mutableListOf<SaleItem>()
                
                for (j in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(j)
                    saleItems.add(
                        SaleItem(
                            inventoryItemId = itemObj.getString("inventoryItemId"),
                            name = itemObj.getString("name"),
                            quantity = itemObj.getInt("quantity"),
                            price = itemObj.getDouble("price"),
                            total = itemObj.getDouble("total")
                        )
                    )
                }
                
                sales.add(
                    Sale(
                        id = obj.getString("id"),
                        customerId = obj.getString("customerId"),
                        customerName = obj.getString("customerName"),
                        date = obj.getString("date"),
                        items = saleItems,
                        totalAmount = obj.getDouble("totalAmount"),
                        paymentMethod = obj.optString("paymentMethod", "Cash"),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return sales
    }

    private fun updateCustomerReport(sale: Sale) {
        val reports = getCustomerReports().toMutableList()
        val existingReport = reports.find { it.customerId == sale.customerId }

        if (existingReport != null) {
            val updatedReport = existingReport.copy(
                totalPurchases = existingReport.totalPurchases + sale.totalAmount,
                lastPurchaseDate = sale.date,
                numberOfPurchases = existingReport.numberOfPurchases + 1,
                totalAmount = existingReport.totalAmount + sale.totalAmount
            )
            reports.remove(existingReport)
            reports.add(updatedReport)
        } else {
            reports.add(CustomerReport(
                customerId = sale.customerId,
                customerName = sale.customerName,
                totalPurchases = sale.totalAmount,
                lastPurchaseDate = sale.date,
                numberOfPurchases = 1,
                totalAmount = sale.totalAmount
            ))
        }

        val json = Gson().toJson(reports)
        sharedPreferences.edit().putString("customer_reports", json).apply()
    }

    fun getInventoryItemById(id: String): InventoryItem? {
        return getInventoryItems().find { it.id == id }
    }

    // Customer methods
    fun getCustomers(): List<Customer> {
        val jsonArray = sharedPreferences.getString("customers", "[]") ?: "[]"
        val customers = mutableListOf<Customer>()
        
        try {
            val array = JSONArray(jsonArray)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                customers.add(
                    Customer(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        email = obj.getString("email"),
                        phone = obj.getString("phone"),
                        address = obj.optString("address", ""),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return customers
    }

    fun saveCustomer(customer: Customer) {
        val customers = getCustomers().toMutableList()
        val index = customers.indexOfFirst { it.id == customer.id }
        
        if (index >= 0) {
            customers[index] = customer
        } else {
            customers.add(customer)
        }
        
        saveCustomers(customers)
    }

    private fun saveCustomers(customers: List<Customer>) {
        val jsonArray = JSONArray()
        
        for (customer in customers) {
            val jsonObject = JSONObject().apply {
                put("id", customer.id)
                put("name", customer.name)
                put("email", customer.email)
                put("phone", customer.phone)
                put("address", customer.address)
                put("notes", customer.notes)
            }
            jsonArray.put(jsonObject)
        }
        
        sharedPreferences.edit().putString("customers", jsonArray.toString()).apply()
    }

    fun getCustomerReports(): List<CustomerReport> {
        val json = sharedPreferences.getString("customer_reports", "[]") ?: "[]"
        return try {
            Gson().fromJson(json, Array<CustomerReport>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}