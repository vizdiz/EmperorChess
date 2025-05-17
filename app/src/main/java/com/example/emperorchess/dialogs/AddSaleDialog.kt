package com.example.emperorchess.dialogs

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.emperorchess.R
import com.example.emperorchess.adapters.SaleItemAdapter
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.models.Customer
import com.example.emperorchess.models.Sale
import com.example.emperorchess.models.SaleItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddSaleDialog : DialogFragment() {
    private lateinit var accountingDatabase: AccountingDatabase
    private lateinit var spinnerCustomer: AutoCompleteTextView
    private lateinit var rvItems: RecyclerView
    private lateinit var btnAddItem: MaterialButton
    private lateinit var tvTotal: TextView
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private lateinit var saleItemAdapter: SaleItemAdapter
    private val saleItems = mutableListOf<SaleItem>()
    private var customers = listOf<Customer>()
    private var totalAmount = 0.0
    private var onSaleAdded: ((Sale) -> Unit)? = null
    private var selectedCustomer: Customer? = null
    private var sale: Sale? = null

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
        return inflater.inflate(R.layout.dialog_add_sale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        loadCustomers()
        setupListeners()
        updateTotalDisplay()
        
        // Update title based on mode
        view.findViewById<TextView>(R.id.dialogTitle).text = 
            if (sale == null) "Add New Sale" else "Edit Sale"
        
        // Pre-fill fields if editing
        sale?.let { existingSale ->
            selectedCustomer = customers.find { it.id == existingSale.customerId }
            selectedCustomer?.let { customer ->
                spinnerCustomer.setText(customer.name, false)
            }
            
            saleItems.addAll(existingSale.items)
            saleItemAdapter.notifyDataSetChanged()
            
            totalAmount = existingSale.totalAmount
            updateTotalDisplay()
            
            etNotes.setText(existingSale.notes)
        }
    }

    private fun initViews(view: View) {
        spinnerCustomer = view.findViewById(R.id.spinnerCustomer)
        rvItems = view.findViewById(R.id.rvItems)
        btnAddItem = view.findViewById(R.id.btnAddItem)
        tvTotal = view.findViewById(R.id.tvTotal)
        etNotes = view.findViewById(R.id.etNotes)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)
    }

    private fun setupRecyclerView() {
        saleItemAdapter = SaleItemAdapter(saleItems)
        saleItemAdapter.setOnDeleteClickListener { position ->
            val item = saleItems[position]
            totalAmount -= item.total
            saleItems.removeAt(position)
            saleItemAdapter.notifyItemRemoved(position)
            updateTotalDisplay()
        }
        
        rvItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = saleItemAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.top = 4
                    outRect.bottom = 4
                    outRect.left = 8
                    outRect.right = 8
                }
            })
        }
    }

    private fun loadCustomers() {
        try {
            customers = accountingDatabase.getCustomers()
            if (customers.isEmpty()) {
                Toast.makeText(context, "Please add customers first", Toast.LENGTH_SHORT).show()
                dismiss()
                return
            }

            val customerNames = customers.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.item_dropdown,
                customerNames
            )
            
            spinnerCustomer.setAdapter(adapter)
            spinnerCustomer.setOnItemClickListener { _, _, position, _ ->
                selectedCustomer = customers[position]
            }

            // Set initial selection
            if (customers.isNotEmpty()) {
                selectedCustomer = customers[0]
                spinnerCustomer.setText(customers[0].name, false)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading customers: ${e.message}", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun setupListeners() {
        btnAddItem.setOnClickListener {
            showAddItemDialog()
        }

        btnSave.setOnClickListener {
            saveSale()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showAddItemDialog() {
        AddSaleItemDialog.newInstance { item ->
            saleItems.add(item)
            saleItemAdapter.notifyItemInserted(saleItems.size - 1)
            totalAmount += item.total
            updateTotalDisplay()
        }.show(childFragmentManager, "add_item")
    }

    private fun updateTotalDisplay() {
        val formatter = NumberFormat.getCurrencyInstance()
        tvTotal.text = "Total: ${formatter.format(totalAmount)}"
    }

    private fun saveSale() {
        if (saleItems.isEmpty()) {
            Toast.makeText(context, "Please add at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        val customer = selectedCustomer
        if (customer == null) {
            Toast.makeText(context, "Please select a customer", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val updatedSale = sale?.copy(
                customerId = customer.id,
                customerName = customer.name,
                items = saleItems.toList(),
                totalAmount = totalAmount,
                notes = etNotes.text?.toString()?.trim() ?: "",
                updatedAt = System.currentTimeMillis()
            ) ?: Sale.createWithCurrentDate(
                customerId = customer.id,
                customerName = customer.name,
                items = saleItems.toList(),
                totalAmount = totalAmount,
                notes = etNotes.text?.toString()?.trim() ?: ""
            )

            btnSave.isEnabled = false
            btnAddItem.isEnabled = false
            
            try {
                accountingDatabase.saveSale(updatedSale)
                onSaleAdded?.invoke(updatedSale)
                Toast.makeText(context, "Sale saved successfully", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                btnSave.isEnabled = true
                btnAddItem.isEnabled = true
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving sale: ${e.message}", Toast.LENGTH_SHORT).show()
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
            sale: Sale? = null,
            onSaleAdded: (Sale) -> Unit
        ): AddSaleDialog {
            return AddSaleDialog().apply {
                this.sale = sale
                this.onSaleAdded = onSaleAdded
            }
        }
    }
} 