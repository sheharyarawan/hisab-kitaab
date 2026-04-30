package com.example.hisabkitaab.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hisabkitaab.R
import com.example.hisabkitaab.data.model.CustomerTransactionUiModel

class CustomerTransactionAdapter(
    private val onItemClick: (CustomerTransactionUiModel) -> Unit
) : RecyclerView.Adapter<CustomerTransactionAdapter.TransactionViewHolder>() {
    private val items = mutableListOf<CustomerTransactionUiModel>()

    fun submitList(newItems: List<CustomerTransactionUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_transaction, parent, false)
        return TransactionViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TransactionViewHolder(
        itemView: View,
        private val onItemClick: (CustomerTransactionUiModel) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val dateView: TextView = itemView.findViewById(R.id.tvItemDate)
        private val noteView: TextView = itemView.findViewById(R.id.tvItemNote)
        private val balanceView: TextView = itemView.findViewById(R.id.tvItemBalance)
        private val diyeView: TextView = itemView.findViewById(R.id.tvItemMaineDiye)
        private val liyeView: TextView = itemView.findViewById(R.id.tvItemMaineLiye)

        fun bind(item: CustomerTransactionUiModel) {
            dateView.text = item.dateText
            noteView.text = item.noteText
            balanceView.text = item.balanceText
            diyeView.text = item.diyeText
            liyeView.text = item.liyeText
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
