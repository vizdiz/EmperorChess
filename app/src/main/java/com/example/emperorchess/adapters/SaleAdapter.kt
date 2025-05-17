package com.example.emperorchess.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.models.Sale
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SaleAdapter(
    private val sales: List<Sale>,
    private val onItemClick: (Sale) -> Unit,
    private val onEditClick: (Sale) -> Unit
) : RecyclerView.Adapter<SaleAdapter.SaleViewHolder>() {

    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val outputDateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance()

    class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale, parent, false)
        return SaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val sale = sales[position]
        
        holder.tvCustomerName.text = sale.customerName
        holder.tvDate.text = sale.getDisplayDate()
        holder.tvTotal.text = numberFormat.format(sale.totalAmount)

        holder.itemView.setOnClickListener {
            onItemClick(sale)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(sale)
        }
    }

    override fun getItemCount(): Int = sales.size
} 