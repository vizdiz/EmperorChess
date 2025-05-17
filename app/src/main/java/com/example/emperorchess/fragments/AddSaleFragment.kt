package com.example.emperorchess.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.adapters.SaleItemAdapter
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.dialogs.AddSaleItemDialog
import com.example.emperorchess.models.Customer
import com.example.emperorchess.models.Sale
import com.example.emperorchess.models.SaleItem
import java.text.NumberFormat
import java.util.*

class AddSaleFragment : DialogFragment() {
    private lateinit var customerSpinner: Spinner
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var addItemButton: Button
    private lateinit var notesInput: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var totalAmountText: TextView
    private lateinit var accountingDatabase: AccountingDatabase
    private lateinit var saleItemAdapter: SaleItemAdapter
    
    private val saleItems = mutableListOf<SaleItem>()
    private var customers = listOf<Customer>()
    private var totalAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_sale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountingDatabase = AccountingDatabase(requireContext())
        
        initializeViews(view)
        setupRecyclerView()
        loadCustomers()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        customerSpinner = view.findViewById(R.id.customerSpinner)
        itemsRecyclerView = view.findViewById(R.id.itemsRecyclerView)
        addItemButton = view.findViewById(R.id.addItemButton)
        notesInput = view.findViewById(R.id.notesInput)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        totalAmountText = view.findViewById(R.id.totalAmountText)
        
        updateTotalAmount(0.0)
    }

    private fun setupRecyclerView() {
        saleItemAdapter = SaleItemAdapter(saleItems)
        
        itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = saleItemAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleItemDeletion(deletedItem: SaleItem) {
        totalAmount -= deletedItem.total
        updateTotalAmount(totalAmount)
        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
    }

    private fun loadCustomers() {
        try {
            customers = accountingDatabase.getCustomers()
            if (customers.isEmpty()) {
                showError("No customers found. Please add a customer first.")
                dismiss()
                return
            }
            
            val customerNames = customers.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                customerNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            customerSpinner.adapter = adapter
        } catch (e: Exception) {
            showError("Failed to load customers: ${e.message}")
            dismiss()
        }
    }

    private fun setupClickListeners() {
        addItemButton.setOnClickListener {
            showAddItemDialog()
        }

        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveSale()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showAddItemDialog() {
        AddSaleItemDialog.newInstance { saleItem ->
            addSaleItem(saleItem)
        }.show(childFragmentManager, "add_sale_item")
    }

    private fun addSaleItem(item: SaleItem) {
        saleItemAdapter.addItem(item)
        totalAmount += item.total
        updateTotalAmount(totalAmount)
    }

    private fun updateTotalAmount(amount: Double) {
        totalAmount = amount
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale.getDefault())
            .format(amount)
        totalAmountText.text = "Total: $formattedAmount"
    }

    private fun validateInputs(): Boolean {
        if (customers.isEmpty()) {
            showError("No customers available")
            return false
        }

        if (saleItems.isEmpty()) {
            showError("Please add at least one item")
            return false
        }

        return true
    }

    private fun saveSale() {
        try {
            val selectedCustomer = customers[customerSpinner.selectedItemPosition]
            val sale = Sale.createWithCurrentDate(
                customerId = selectedCustomer.id,
                customerName = selectedCustomer.name,
                items = saleItems.toList(),
                totalAmount = totalAmount,
                notes = notesInput.text.toString().trim()
            )

            accountingDatabase.saveSale(sale)
            Toast.makeText(context, "Sale saved successfully", Toast.LENGTH_SHORT).show()
            dismiss()
        } catch (e: Exception) {
            showError("Failed to save sale: ${e.message}")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = AddSaleFragment()
    }
} 