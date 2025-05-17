package com.example.emperorchess.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.emperorchess.R
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.models.Customer
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class AddCustomerDialog : DialogFragment() {
    private lateinit var accountingDatabase: AccountingDatabase
    private lateinit var nameInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    
    private var customer: Customer? = null
    private var onCustomerSaved: ((Customer) -> Unit)? = null

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
        return inflater.inflate(R.layout.dialog_add_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        
        // Update title based on mode
        view.findViewById<TextView>(R.id.dialogTitle).text =
            if (customer == null) "Add New Customer" else "Edit Customer"
        
        // Pre-fill fields if editing
        customer?.let { customer ->
            nameInput.setText(customer.name)
            phoneInput.setText(customer.phone)
            emailInput.setText(customer.email)
        }
    }

    private fun initViews(view: View) {
        nameInput = view.findViewById(R.id.nameInput)
        phoneInput = view.findViewById(R.id.phoneInput)
        emailInput = view.findViewById(R.id.emailInput)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveCustomer()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveCustomer() {
        val name = nameInput.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            return
        }

        val phone = phoneInput.text?.toString()?.trim() ?: ""
        val email = emailInput.text?.toString()?.trim() ?: ""

        try {
            val updatedCustomer = customer?.copy(
                name = name,
                phone = phone,
                email = email
            ) ?: Customer(
                name = name,
                phone = phone,
                email = email
            )

            accountingDatabase.saveCustomer(updatedCustomer)
            onCustomerSaved?.invoke(updatedCustomer)
            dismiss()
            Toast.makeText(context, "Customer saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving customer: ${e.message}", Toast.LENGTH_SHORT).show()
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
            customer: Customer? = null,
            onCustomerSaved: (Customer) -> Unit
        ): AddCustomerDialog {
            return AddCustomerDialog().apply {
                this.customer = customer
                this.onCustomerSaved = onCustomerSaved
            }
        }
    }
} 