package com.example.emperorchess

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.adapters.InventoryAdapter
import com.example.emperorchess.models.InventoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InventoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: View
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var adapter: InventoryAdapter
    private val items = mutableListOf<InventoryItem>()
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "inventory_prefs"
        private const val KEY_ITEMS = "inventory_items"
        const val REQUEST_ADD_ITEM = 1
        const val REQUEST_EDIT_ITEM = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Inventory"

        initViews()
        loadItems()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAddItem = findViewById(R.id.fabAddItem)
    }

    private fun loadItems() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(KEY_ITEMS, null)
        if (json != null) {
            val type = object : TypeToken<List<InventoryItem>>() {}.type
            items.clear()
            items.addAll(gson.fromJson(json, type))
        }
    }

    private fun saveItems() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_ITEMS, json).apply()
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(items) { item ->
            val intent = Intent(this, EditInventoryItemActivity::class.java).apply {
                putExtra("item", gson.toJson(item))
                putExtra("position", items.indexOf(item))
            }
            startActivityForResult(intent, REQUEST_EDIT_ITEM)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        updateEmptyState()
    }

    private fun setupClickListeners() {
        fabAddItem.setOnClickListener {
            val intent = Intent(this, EditInventoryItemActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_ITEM)
        }
    }

    private fun updateEmptyState() {
        if (items.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_ADD_ITEM -> {
                    val itemJson = data.getStringExtra("item")
                    val item = gson.fromJson(itemJson, InventoryItem::class.java)
                    items.add(item)
                    adapter.notifyItemInserted(items.size - 1)
                }
                REQUEST_EDIT_ITEM -> {
                    val itemJson = data.getStringExtra("item")
                    val item = gson.fromJson(itemJson, InventoryItem::class.java)
                    val position = data.getIntExtra("position", -1)
                    if (position != -1) {
                        items[position] = item
                        adapter.notifyItemChanged(position)
                    }
                }
            }
            saveItems()
            updateEmptyState()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 