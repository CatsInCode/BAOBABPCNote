package com.example.bar.ui.crm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bar.R
import com.example.bar.databinding.ItemOrderBinding

class OrdersAdapter(
    private val onEditClick: (OrderResponse) -> Unit,
    private val onDeleteClick: (OrderResponse) -> Unit,
    private val currentRole: String, // Добавляем текущую роль
    private val currentUserId: Int? // Добавляем текущий ID пользователя
) : ListAdapter<OrderResponse, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    // OrdersAdapter.kt - обновим отображение статуса
    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderResponse) {
            binding.apply {
                // Для всех ролей показываем ID заказа
                tvOrderId.text = "Order #${order.orderId}"

                // Для модераторов/админов добавляем информацию о создателе
                if (currentRole in listOf("moder", "admin")) {
                    tvCreator.text = "Created by: ${order.username ?: "Unknown"}"
                    tvCreator.visibility = View.VISIBLE
                } else {
                    tvCreator.visibility = View.GONE
                }

                // Если заказ в обработке, показываем кто обрабатывает
                if (order.status != "new" && order.processedBy != null) {
                    tvProcessor.text = if (order.processedBy == currentUserId) {
                        "Сontractor : You"
                    } else {
                        "Сontractor : ${order.processedBy}"
                    }
                    tvCreator.text =  "Created by: ${order.username}"
                    tvCreator.visibility = View.VISIBLE
                    tvProcessor.visibility = View.VISIBLE
                } else {
                    tvCreator.visibility = View.GONE
                    tvProcessor.visibility = View.GONE
                }

                tvBuildId.text = "Build: ${order.buildDescription ?: order.buildId}"
                tvStatus.text = "Status: ${order.status.capitalize()}"

                val statusColor = when (order.status) {
                    "new" -> R.color.status_new
                    "processing" -> R.color.status_processing
                    "completed" -> R.color.status_completed
                    "cancelled" -> R.color.status_cancelled
                    else -> android.R.color.darker_gray
                }
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

                btnEdit.setOnClickListener { onEditClick(order) }
                btnDelete.setOnClickListener { onDeleteClick(order) }
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