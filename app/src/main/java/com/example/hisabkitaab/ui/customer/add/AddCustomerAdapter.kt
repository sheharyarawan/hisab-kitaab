package com.example.hisabkitaab.ui.customer.add

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.model.ContactUiModel

class AddCustomerAdapter(
    private val onAddClick: (ContactUiModel) -> Unit
) : ListAdapter<ContactUiModel, AddCustomerAdapter.ContactViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ContactUiModel>() {
        override fun areItemsTheSame(oldItem: ContactUiModel, newItem: ContactUiModel): Boolean {
            return oldItem.phone == newItem.phone
        }

        override fun areContentsTheSame(oldItem: ContactUiModel, newItem: ContactUiModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_customer_contact, parent, false)
        return ContactViewHolder(view, onAddClick)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ContactViewHolder(
        itemView: View,
        private val onAddClick: (ContactUiModel) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val initialsView: TextView = itemView.findViewById(R.id.tvInitialCircle)
        private val nameView: TextView = itemView.findViewById(R.id.tvContactName)
        private val phoneView: TextView = itemView.findViewById(R.id.tvContactPhone)
        private val addActionView: TextView = itemView.findViewById(R.id.tvAddAction)

        fun bind(item: ContactUiModel) {
            initialsView.text = item.initials
            nameView.text = item.displayName
            phoneView.text = item.phone

            if (item.isAdded) {
                addActionView.text = "ADDED"
                addActionView.setTextColor(Color.parseColor("#9AA7B1"))
                addActionView.setOnClickListener(null)
            } else {
                addActionView.text = "+  ADD"
                addActionView.setTextColor(Color.parseColor("#005FCB"))
                addActionView.setOnClickListener { onAddClick(item) }
            }
        }
    }
}
