package com.example.hisabkitaab.ui.customer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.model.CustomerBalance
import kotlin.math.abs

class CustomerAdapter(
    private val onItemClick: (CustomerBalance) -> Unit,
    private val onItemLongClick: (CustomerBalance) -> Unit
) : ListAdapter<CustomerBalance, CustomerAdapter.CustomerViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<CustomerBalance>() {
        override fun areItemsTheSame(oldItem: CustomerBalance, newItem: CustomerBalance): Boolean {
            return oldItem.customerId == newItem.customerId
        }

        override fun areContentsTheSame(oldItem: CustomerBalance, newItem: CustomerBalance): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CustomerViewHolder(
        itemView: View,
        private val onItemClick: (CustomerBalance) -> Unit,
        private val onItemLongClick: (CustomerBalance) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val initialsView: TextView = itemView.findViewById(R.id.tvInitials)
        private val nameView: TextView = itemView.findViewById(R.id.customerName)
        private val amountView: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(item: CustomerBalance) {
            initialsView.text = item.name.split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifBlank { "?" }
            nameView.text = item.name

            val sign = when {
                item.netAmount > 0 -> "+"
                item.netAmount < 0 -> "-"
                else -> ""
            }
            amountView.text = "Rs. $sign${abs(item.netAmount).toInt()}"
            amountView.setTextColor(
                Color.parseColor(if (item.netAmount >= 0) "#1B9C5A" else "#E53935")
            )

            itemView.setOnClickListener {
                onItemClick(item)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}