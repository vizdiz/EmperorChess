package com.example.emperorchess.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.models.Customer

class CustomerAdapter(
    private val customers: List<Customer>,
    private val onItemClick: (Customer) -> Unit,
    private val onEditClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvName.text = customer.name
        holder.tvEmail.text = customer.email
        holder.tvPhone.text = customer.phone

        holder.itemView.setOnClickListener {
            onItemClick(customer)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(customer)
        }
    }

    override fun getItemCount(): Int = customers.size
} 