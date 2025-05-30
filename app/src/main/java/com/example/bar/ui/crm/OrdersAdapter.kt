package com.example.bar.ui.crm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bar.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(
    private val onEditClick: (OrderResponse) -> Unit,
    private val onDeleteClick: (OrderResponse) -> Unit
) : ListAdapter<OrderResponse, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderResponse) {
            binding.apply {
                tvOrderId.text = "Order #${order.orderId}"
                tvBuildId.text = "Build: ${order.buildId}"
                tvStatus.text = "Status: ${order.status}"
                tvDate.text = formatDate(order.createdAt)

                btnEdit.setOnClickListener { onEditClick(order) }
                btnDelete.setOnClickListener { onDeleteClick(order) }

                // Показываем кнопки только для модераторов и админов
                val isModerOrAdmin = true // Здесь нужно проверять роль пользователя
                btnEdit.visibility = if (isModerOrAdmin) View.VISIBLE else View.GONE
                btnDelete.visibility = if (isModerOrAdmin) View.VISIBLE else View.GONE
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateString
            }
        }
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<OrderResponse>() {
    override fun areItemsTheSame(oldItem: OrderResponse, newItem: OrderResponse): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: OrderResponse, newItem: OrderResponse): Boolean {
        return oldItem == newItem
    }
}