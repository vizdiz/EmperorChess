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
import com.example.emperorchess.adapters.SaleAdapter
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.dialogs.AddSaleDialog
import com.example.emperorchess.models.Customer
import com.example.emperorchess.models.Sale
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class SalesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddSale: FloatingActionButton
    private lateinit var adapter: SaleAdapter
    private lateinit var database: AccountingDatabase
    private val sales = mutableListOf<Sale>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupDatabase()
        setupRecyclerView()
        loadSales()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddSale = view.findViewById(R.id.fabAddSale)
    }

    private fun setupDatabase() {
        database = AccountingDatabase(requireContext())
    }

    private fun setupRecyclerView() {
        adapter = SaleAdapter(
            sales,
            onItemClick = { sale ->
                showSaleDetails(sale)
            },
            onEditClick = { sale ->
                showEditSaleDialog(sale)
            }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@SalesFragment.adapter
        }
    }

    private fun loadSales() {
        sales.clear()
        sales.addAll(database.getSales().sortedByDescending { it.date })
        updateEmptyState()
        adapter.notifyDataSetChanged()
    }

    private fun updateEmptyState() {
        if (sales.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        fabAddSale.setOnClickListener {
            showAddSaleDialog()
        }
    }

    private fun showAddSaleDialog() {
        AddSaleDialog.newInstance { newSale ->
            // Add the new sale to the list
            sales.add(0, newSale) // Add to the beginning since it's the newest
            updateEmptyState()
            adapter.notifyItemInserted(0)
            recyclerView.scrollToPosition(0) // Scroll to show the new sale
        }.show(childFragmentManager, "add_sale")
    }

    private fun showSaleDetails(sale: Sale) {
        // TODO: Implement sale details view
        Toast.makeText(context, "Sale details coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showEditSaleDialog(sale: Sale) {
        AddSaleDialog.newInstance(
            sale = sale,
            onSaleAdded = { updatedSale ->
                val position = sales.indexOfFirst { it.id == updatedSale.id }
                if (position != -1) {
                    sales[position] = updatedSale
                    adapter.notifyItemChanged(position)
                }
            }
        ).show(childFragmentManager, "edit_sale")
    }

    // Call this method when returning to the fragment
    override fun onResume() {
        super.onResume()
        loadSales() // Refresh the sales list
    }
} 