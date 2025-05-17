package com.example.emperorchess.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.models.SaleItem
import java.text.NumberFormat
import java.util.*

class SaleItemAdapter(
    private val items: MutableList<SaleItem>
) : RecyclerView.Adapter<SaleItemAdapter.ViewHolder>() {
    
    private var onDeleteClick: ((Int) -> Unit)? = null
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.apply {
            tvItemName.text = item.name
            tvQuantity.text = "Qty: ${item.quantity}"
            tvPrice.text = "@ ${numberFormat.format(item.price)}"
            tvTotal.text = numberFormat.format(item.total)

            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(position)
            }
        }
    }

    override fun getItemCount() = items.size

    fun setOnDeleteClickListener(listener: (Int) -> Unit) {
        onDeleteClick = listener
    }

    fun addItem(item: SaleItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        if (position in 0 until items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }

    fun getItems(): List<SaleItem> = items.toList()

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }
}