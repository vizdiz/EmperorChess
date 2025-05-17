package com.example.emperorchess.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.emperorchess.R
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.models.InventoryItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class AddInventoryDialog : DialogFragment() {
    private lateinit var etName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etStock: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var accountingDatabase: AccountingDatabase
    
    private var inventoryItem: InventoryItem? = null
    private var onItemSaved: ((InventoryItem) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.EmperorDialog)
        accountingDatabase = AccountingDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_inventory_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        prefillData()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initViews(view: View) {
        etName = view.findViewById(R.id.etName)
        etDescription = view.findViewById(R.id.etDescription)
        etPrice = view.findViewById(R.id.etPrice)
        etStock = view.findViewById(R.id.etStock)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveItem()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun prefillData() {
        inventoryItem?.let { item ->
            etName.setText(item.name)
            etDescription.setText(item.description)
            etPrice.setText(item.price.toString())
            etStock.setText(item.stock.toString())
        }
    }

    private fun saveItem() {
        val name = etName.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }

        val price = etPrice.text?.toString()?.toDoubleOrNull()
        if (price == null || price <= 0) {
            etPrice.error = "Valid price is required"
            return
        }

        val stock = etStock.text?.toString()?.toIntOrNull()
        if (stock == null || stock < 0) {
            etStock.error = "Valid stock quantity is required"
            return
        }

        try {
            val updatedItem = inventoryItem?.copy(
                name = name,
                description = etDescription.text?.toString()?.trim() ?: "",
                price = price,
                stock = stock,
                updatedAt = System.currentTimeMillis()
            ) ?: InventoryItem(
                id = UUID.randomUUID().toString(),
                name = name,
                description = etDescription.text?.toString()?.trim() ?: "",
                price = price,
                stock = stock
            )

            accountingDatabase.saveInventoryItem(updatedItem)
            onItemSaved?.invoke(updatedItem)
            dismiss()
            Toast.makeText(context, "Item saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving item: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(
            item: InventoryItem? = null,
            onItemSaved: (InventoryItem) -> Unit
        ): AddInventoryDialog {
            return AddInventoryDialog().apply {
                this.inventoryItem = item
                this.onItemSaved = onItemSaved
            }
        }
    }
} 