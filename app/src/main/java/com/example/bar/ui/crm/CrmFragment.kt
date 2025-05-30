package com.example.bar.ui.crm

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bar.R
import com.example.bar.databinding.DialogOrderBinding
import com.example.bar.databinding.FragmentCrmBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrmFragment : Fragment() {
    private var _binding: FragmentCrmBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private var authToken: String? = null
    private var currentRole: String = "user"
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiService.create()
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(
            onEditClick = { order -> showOrderDialog(order) },
            onDeleteClick = { order -> deleteOrder(order.orderId) }
        )

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                showToast("Please fill all fields")
            }
        }

        binding.btnCreateOrder.setOnClickListener {
            showOrderDialog()
        }

        binding.btnManageUsers.setOnClickListener {
            // Реализация управления пользователями
            showToast("User management will be implemented here")
        }

        binding.btnViewAllOrders.setOnClickListener {
            loadAllOrders()
        }
    }

    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                authToken = response.token
                currentRole = response.role // Предполагаем, что API возвращает роль

                withContext(Dispatchers.Main) {
                    showToast("Login successful")
                    binding.loginSection.visibility = View.GONE // <- Скрываем плашку логина
                    updateUIForRole()
                    loadUserOrders()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Login failed: ${e.message}")
                    updateStatus("Login error: ${e.message}")
                }
            }
        }
    }

    private fun updateUIForRole() {
        binding.ordersSection.visibility = View.VISIBLE

        when (currentRole) {
            "admin" -> {
                binding.adminSection.visibility = View.VISIBLE
                binding.btnCreateOrder.isEnabled = true
            }
            "moder" -> {
                binding.adminSection.visibility = View.VISIBLE
                binding.btnManageUsers.isEnabled = false
                binding.btnCreateOrder.isEnabled = true
            }
            else -> {
                binding.adminSection.visibility = View.GONE
                binding.btnCreateOrder.isEnabled = true
            }
        }
    }

    private fun showOrderDialog(order: OrderResponse? = null) {
        val dialogBinding = DialogOrderBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(if (order == null) "Create Order" else "Edit Order")
            .create()

        order?.let {
            dialogBinding.etBuildId.setText(it.buildId)
            dialogBinding.etPhone.setText(it.contactPhone)
            dialogBinding.etAddress.setText(it.deliveryAddress)
            dialogBinding.etNotes.setText(it.notes)
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val buildId = dialogBinding.etBuildId.text.toString()
            val phone = dialogBinding.etPhone.text.toString()
            val address = dialogBinding.etAddress.text.toString()
            val notes = dialogBinding.etNotes.text.toString()

            if (buildId.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                val orderRequest = OrderRequest(buildId, phone, address, notes.ifEmpty { null })

                if (order == null) {
                    createOrder(orderRequest)
                } else {
                    updateOrder(order.orderId, orderRequest)
                }
                dialog.dismiss()
            } else {
                showToast("Please fill required fields")
            }
        }

        dialog.show()
    }

    private fun createOrder(orderRequest: OrderRequest) {
        val token = authToken ?: run {
            showToast("Not authorized")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.createOrder("Bearer $token", orderRequest)
                withContext(Dispatchers.Main) {
                    showToast("Order created!")
                    loadUserOrders()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Order failed: ${e.message}")
                }
            }
        }
    }

    private fun updateOrder(orderId: Int, orderRequest: OrderRequest) {
        val token = authToken ?: run {
            showToast("Not authorized")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateOrder("Bearer $token", orderId, orderRequest)
                withContext(Dispatchers.Main) {
                    showToast("Order updated!")
                    loadUserOrders()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Update failed: ${e.message}")
                }
            }
        }
    }

    private fun deleteOrder(orderId: Int) {
        val token = authToken ?: run {
            showToast("Not authorized")
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this order?")
            .setPositiveButton("Delete") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        apiService.deleteOrder("Bearer $token", orderId)
                        withContext(Dispatchers.Main) {
                            showToast("Order deleted")
                            loadUserOrders()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showToast("Delete failed: ${e.message}")
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadUserOrders() {
        val token = authToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val orders = apiService.getUserOrders("Bearer $token")
                withContext(Dispatchers.Main) {
                    ordersAdapter.submitList(orders)
                    updateStatus("Loaded ${orders.size} orders")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateStatus("Error loading orders: ${e.message}")
                }
            }
        }
    }

    private fun loadAllOrders() {
        val token = authToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val orders = apiService.getAllOrders("Bearer $token")
                withContext(Dispatchers.Main) {
                    ordersAdapter.submitList(orders)
                    updateStatus("Loaded ${orders.size} orders (all)")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateStatus("Error loading all orders: ${e.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateStatus(message: String) {
        binding.tvStatus.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}