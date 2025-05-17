package com.example.emperorchess.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.adapters.InventoryAdapter
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.models.InventoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InventoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var adapter: InventoryAdapter
    private lateinit var database: AccountingDatabase
    private var items: MutableList<InventoryItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddItem = view.findViewById(R.id.fabAddItem)

        // Initialize database
        database = AccountingDatabase(requireContext())

        // Set up RecyclerView
        adapter = InventoryAdapter(items) { item ->
            showEditItemDialog(item)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Set up FAB
        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }

        // Load items
        loadItems()
    }

    private fun loadItems() {
        items.clear()
        items.addAll(database.getInventoryItems())
        adapter.notifyDataSetChanged()

        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showAddItemDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_inventory_item)

        val nameInput = dialog.findViewById<EditText>(R.id.etName)
        val priceInput = dialog.findViewById<EditText>(R.id.etPrice)
        val stockInput = dialog.findViewById<EditText>(R.id.etStock)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

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

                val item = InventoryItem(
                    name = name,
                    price = price,
                    stock = stock
                )

                database.saveInventoryItem(item)
                loadItems()
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditItemDialog(item: InventoryItem) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_inventory_item)

        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val nameInput = dialog.findViewById<EditText>(R.id.nameInput)
        val priceInput = dialog.findViewById<EditText>(R.id.etPrice)
        val stockInput = dialog.findViewById<EditText>(R.id.etStock)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = "Edit Inventory Item"
        nameInput.setText(item.name)
        priceInput.setText(item.price.toString())
        stockInput.setText(item.stock.toString())

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

                val updatedItem = item.copy(
                    name = name,
                    price = price,
                    stock = stock
                )

                database.saveInventoryItem(updatedItem)
                loadItems()
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
} 