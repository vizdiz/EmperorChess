package com.example.emperorchess

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.emperorchess.models.InventoryItem
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import java.util.UUID

class EditInventoryItemActivity : AppCompatActivity() {
    private lateinit var etName: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etStock: TextInputEditText
    private lateinit var btnSave: Button
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_inventory_item)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Item"

        initViews()
        loadItemData()
        setupClickListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etPrice = findViewById(R.id.etPrice)
        etStock = findViewById(R.id.etStock)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun loadItemData() {
        val itemJson = intent.getStringExtra("item")
        if (itemJson != null) {
            val item = gson.fromJson(itemJson, InventoryItem::class.java)
            etName.setText(item.name)
            etPrice.setText(item.price.toString())
            etStock.setText(item.stock.toString())
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateInput()) {
                saveItem()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = etName.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val stockStr = etStock.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            return false
        }

        if (priceStr.isEmpty()) {
            etPrice.error = "Price is required"
            return false
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            etPrice.error = "Invalid price"
            return false
        }

        if (stockStr.isEmpty()) {
            etStock.error = "Stock is required"
            return false
        }

        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            etStock.error = "Invalid stock"
            return false
        }

        return true
    }

    private fun saveItem() {
        val name = etName.text.toString().trim()
        val price = etPrice.text.toString().trim().toDouble()
        val stock = etStock.text.toString().trim().toInt()

        val item = intent.getStringExtra("item")?.let { json ->
            // Update existing item using copy()
            val existingItem = gson.fromJson(json, InventoryItem::class.java)
            existingItem.copy(
                name = name,
                price = price,
                stock = stock,
                id = existingItem.id
            )
        } ?: InventoryItem( // Create new item
            id = UUID.randomUUID().toString(),
            name = name,
            price = price,
            stock = stock
        )

        val itemJson = gson.toJson(item)
        intent.putExtra("item", itemJson)
        
        if (intent.hasExtra("position")) {
            intent.putExtra("position", intent.getIntExtra("position", -1))
        }

        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 