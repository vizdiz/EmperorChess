package com.example.emperorchess.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.emperorchess.R
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.models.InventoryItem
import com.example.emperorchess.models.SaleItem
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.*

class AddSaleItemDialog : DialogFragment() {
    private lateinit var db: AccountingDatabase
    private lateinit var actvItem: AutoCompleteTextView
    private lateinit var etQuantity: TextInputEditText
    private lateinit var tvPrice: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button

    private var inventoryItems = listOf<InventoryItem>()
    private var selectedItem: InventoryItem? = null
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private var onItemAddedListener: ((SaleItem) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog_MinWidth)
        db = AccountingDatabase(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_add_sale_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadInventoryItems()
        setupListeners()
    }

    private fun initViews(view: View) {
        actvItem = view.findViewById(R.id.actvItem)
        etQuantity = view.findViewById(R.id.etQuantity)
        tvPrice = view.findViewById(R.id.tvPrice)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnCancel = view.findViewById(R.id.btnCancel)
    }

    private fun loadInventoryItems() {
        inventoryItems = db.getInventoryItems().filter { it.stock > 0 }
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            inventoryItems.map { it.name }
        )
        actvItem.setAdapter(adapter)
    }

    private fun setupListeners() {
        actvItem.setOnItemClickListener { _, _, position, _ ->
            selectedItem = inventoryItems[position]
            updatePriceAndTotal()
        }

        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePriceAndTotal()
            }
        })

        btnAdd.setOnClickListener {
            addItem()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun updatePriceAndTotal() {
        selectedItem?.let { item ->
            tvPrice.text = "Price: ${numberFormat.format(item.price)}"
            
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
            val total = item.price * quantity
            tvTotal.text = "Total: ${numberFormat.format(total)}"
            
            btnAdd.isEnabled = quantity > 0 && quantity <= item.stock
        }
    }

    private fun addItem() {
        val selectedItem = this.selectedItem ?: return
        val quantityStr = etQuantity.text.toString()
        val quantity = quantityStr.toIntOrNull() ?: return

        if (quantity <= 0 || quantity > selectedItem.stock) {
            etQuantity.error = "Invalid quantity"
            return
        }

        val saleItem = SaleItem(
            inventoryItemId = selectedItem.id,
            name = selectedItem.name,
            quantity = quantity,
            price = selectedItem.price,
            total = selectedItem.price * quantity
        )

        onItemAddedListener?.invoke(saleItem)
        dismiss()
    }

    companion object {
        fun newInstance(onItemAdded: (SaleItem) -> Unit): AddSaleItemDialog {
            return AddSaleItemDialog().apply {
                this.onItemAddedListener = onItemAdded
            }
        }
    }
} 