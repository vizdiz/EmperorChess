package com.example.emperorchess.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.adapters.CustomerAdapter
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.dialogs.AddCustomerDialog
import com.example.emperorchess.models.Customer
import com.example.emperorchess.models.Sale
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CustomersFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddCustomer: FloatingActionButton
    private lateinit var adapter: CustomerAdapter
    private lateinit var database: AccountingDatabase
    private val customers = mutableListOf<Customer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupDatabase()
        loadCustomers()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddCustomer = view.findViewById(R.id.fabAddCustomer)
    }

    private fun setupDatabase() {
        database = AccountingDatabase(requireContext())
    }

    private fun loadCustomers() {
        customers.clear()
        customers.addAll(database.getCustomers())
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(
            customers,
            onItemClick = { customer ->
                // Handle item click (view details, etc.)
            },
            onEditClick = { customer ->
                showEditCustomerDialog(customer)
            }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@CustomersFragment.adapter
        }
    }

    private fun setupClickListeners() {
        fabAddCustomer.setOnClickListener {
            showAddCustomerDialog()
        }
    }

    private fun updateEmptyState() {
        if (customers.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    private fun showAddCustomerDialog() {
        AddCustomerDialog.newInstance { customer ->
            // Refresh the customers list
            loadCustomers()
            Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT).show()
        }.show(childFragmentManager, "add_customer")
    }

    private fun showEditCustomerDialog(customer: Customer) {
        AddCustomerDialog.newInstance(
            customer = customer,
            onCustomerSaved = { updatedCustomer ->
                val position = customers.indexOfFirst { it.id == updatedCustomer.id }
                if (position != -1) {
                    customers[position] = updatedCustomer
                    adapter.notifyItemChanged(position)
                }
            }
        ).show(childFragmentManager, "edit_customer")
    }
} 