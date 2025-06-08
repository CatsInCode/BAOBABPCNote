package com.example.bar.ui.crm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private var currentUserId: Int? = null
    private var allOrders: List<OrderResponse> = emptyList()


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
            onDeleteClick = { order -> deleteOrder(order.orderId) },
            currentRole = currentRole,
            currentUserId = currentUserId
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

        binding.btnFilterOrders.setOnClickListener {
            showStatusFilterDialog()
        }
    }
    private fun showStatusFilterDialog() {
        val statuses = arrayOf("All", "new", "processing", "completed", "cancelled")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by status")
            .setItems(statuses) { _, which ->
                val status = if (which == 0) null else statuses[which]
                loadFilteredOrders(status)
            }
            .show()
    }

    private fun loadFilteredOrders(status: String?) {
        val filtered = if (status == null) {
            allOrders
        } else {
            allOrders.filter { it.status == status }
        }

        ordersAdapter.submitList(filtered)
        updateStatus("Filtered ${filtered.size} ${status ?: "all"} orders")
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

            // Показываем выбор статуса только для админов и модераторов
            if (currentRole in listOf("admin", "moder")) {
                dialogBinding.statusGroup.visibility = View.VISIBLE
                when (it.status) {
                    "new" -> dialogBinding.rbNew.isChecked = true
                    "processing" -> dialogBinding.rbProcessing.isChecked = true
                    "completed" -> dialogBinding.rbCompleted.isChecked = true
                    "cancelled" -> dialogBinding.rbCancelled.isChecked = true
                }
            } else {
                dialogBinding.statusGroup.visibility = View.GONE
            }
        } ?: run {
            dialogBinding.statusGroup.visibility = View.GONE
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val buildId = dialogBinding.etBuildId.text.toString()
            val phone = dialogBinding.etPhone.text.toString()
            val address = dialogBinding.etAddress.text.toString()
            val notes = dialogBinding.etNotes.text.toString()
            val status = when (dialogBinding.statusGroup.checkedRadioButtonId) {
                R.id.rbNew -> "new"
                R.id.rbProcessing -> "processing"
                R.id.rbCompleted -> "completed"
                R.id.rbCancelled -> "cancelled"
                else -> null
            }

            if (buildId.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                if (order == null) {
                    val orderRequest = OrderRequest(buildId, phone, address, notes.ifEmpty { null })
                    createOrder(orderRequest)
                } else {
                    val updateRequest = OrderUpdateRequest(
                        buildId = buildId,
                        status = status ?: order.status,
                        contactPhone = phone,
                        deliveryAddress = address,
                        notes = notes.ifEmpty { null }
                    )
                    updateOrder(order.orderId, updateRequest)
                }
                dialog.dismiss()
            } else {
                showToast("Please fill required fields")
            }
        }

        dialog.show()
    }

    private fun setupUserManagement() {
        binding.btnManageUsers.setOnClickListener {
            if (currentRole in listOf("admin", "moder")) {
                loadAllUsers()
            } else {
                showToast("Access denied")
            }
        }
    }

    private fun loadAllUsers() {
        val token = authToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = apiService.getAllUsers("Bearer $token")
                withContext(Dispatchers.Main) {
                    showUserManagementDialog(users)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error loading users: ${e.message}")
                }
            }
        }
    }

    private fun showUserManagementDialog(users: List<UserProfileResponse>) {
        val userNames = users.map { "${it.username} (${it.role})" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage Users")
            .setItems(userNames) { _, which ->
                val selectedUser = users[which]
                showUserActionsDialog(selectedUser)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUserActionsDialog(user: UserProfileResponse) {
        val actions = mutableListOf<String>()
        if (user.role != "admin" && currentRole == "admin") {
            actions.add("Change Role")
        }
        if (!user.isBanned) {
            actions.add("Ban User")
        } else {
            actions.add("Unban User")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage ${user.username}")
            .setItems(actions.toTypedArray()) { _, which ->
                when (actions[which]) {
                    "Change Role" -> showRoleChangeDialog(user)
                    "Ban User" -> banUser(user.id, true)
                    "Unban User" -> banUser(user.id, false)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRoleChangeDialog(user: UserProfileResponse) {
        val roles = arrayOf("user", "moder", "admin")
        val currentRoleIndex = roles.indexOf(user.role)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Role for ${user.username}")
            .setSingleChoiceItems(roles, currentRoleIndex) { _, _ -> }
            .setPositiveButton("Save") { dialog, _ ->
                val selectedRole = roles[(dialog as AlertDialog).listView.checkedItemPosition]
                changeUserRole(user.id, selectedRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun banUser(userId: Int, ban: Boolean) {
        val token = authToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.banUser("Bearer $token", userId)
                withContext(Dispatchers.Main) {
                    showToast(if (ban) "User banned" else "User unbanned")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun changeUserRole(userId: Int, role: String) {
        val token = authToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.changeUserRole("Bearer $token", userId, role)
                withContext(Dispatchers.Main) {
                    showToast("Role changed to $role")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun getCurrentUserId(): Int {
        return currentUserId ?: throw IllegalStateException("User ID is not available. User is not logged in.")
    }

    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                authToken = response.token
                currentRole = response.role

                // Получаем ID пользователя из профиля
                val profile = apiService.getMyProfile("Bearer ${response.token}")
                currentUserId = profile.id // Сохраняем ID пользователя

                withContext(Dispatchers.Main) {
                    showToast("Login successful")
                    binding.loginSection.visibility = View.GONE
                    updateUIForRole()
                    loadUserOrders()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Login failed: ${e.message}")
                    updateStatus("Login error: ${e.message}")
                    binding.tvStatus.text = e.message
                }
            }
        }
    }

    private fun updateUIForRole() {
        binding.ordersSection.visibility = View.VISIBLE
        showToast(currentRole);
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
                    binding.tvStatus.text = e.message
                }
            }
        }
    }

    private fun updateOrder(orderId: Int, orderRequest: OrderUpdateRequest) {
        val token = authToken ?: run {
            showToast("Not authorized")
            return
        }

        // Если пользователь - модератор/админ и меняет статус на "processing",
        // отмечаем что он взял заявку в работу
        if (currentRole in listOf("moder", "admin") && orderRequest.status == "processing") {
            try {
                orderRequest.processedBy = getCurrentUserId()
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
                return
            }
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
                    binding.tvStatus.text = e.message
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
                            binding.tvStatus.text = e.message
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
                    allOrders = orders // сохраняем
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
                val orders = when (currentRole) {
                    "moder" -> apiService.getModeratorOrders("Bearer $token")
                    "admin" -> apiService.getAllOrders("Bearer $token")
                    else -> apiService.getUserOrders("Bearer $token")
                }
                withContext(Dispatchers.Main) {
                    allOrders = orders // сохраняем
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