package com.example.emperorchess.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.emperorchess.R
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.models.InventoryItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddInventoryItemDialog : DialogFragment() {
    private var item: InventoryItem? = null
    private lateinit var accountingDatabase: AccountingDatabase
    private var onItemSaved: ((InventoryItem) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog_MinWidth)
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

        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val nameInput = view.findViewById<TextInputEditText>(R.id.etName)
        val priceInput = view.findViewById<TextInputEditText>(R.id.etPrice)
        val stockInput = view.findViewById<TextInputEditText>(R.id.etStock)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        // Set title based on mode
        dialogTitle.text = if (item == null) "Add Inventory Item" else "Edit Inventory Item"

        // Pre-fill fields if editing
        item?.let {
            nameInput.setText(it.name)
            priceInput.setText(it.price.toString())
            stockInput.setText(it.stock.toString())
        }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString()
            val priceStr = priceInput.text.toString()
            val stockStr = stockInput.text.toString()

            if (name.isBlank() || priceStr.isBlank() || stockStr.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val price = priceStr.toDouble()
                val stock = stockStr.toInt()

                val updatedItem = item?.copy(
                    name = name,
                    price = price,
                    stock = stock,
                    updatedAt = System.currentTimeMillis()
                ) ?: InventoryItem(
                    name = name,
                    price = price,
                    stock = stock
                )

                accountingDatabase.saveInventoryItem(updatedItem)
                onItemSaved?.invoke(updatedItem)
                dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance(
            item: InventoryItem? = null,
            onItemSaved: (InventoryItem) -> Unit
        ): AddInventoryItemDialog {
            return AddInventoryItemDialog().apply {
                this.item = item
                this.onItemSaved = onItemSaved
            }
        }
    }
} 