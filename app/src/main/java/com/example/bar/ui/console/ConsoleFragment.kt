package com.example.bar.ui.console

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bar.databinding.FragmentConsoleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ConsoleFragment : Fragment() {

    private var _binding: FragmentConsoleBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsoleBinding.inflate(inflater, container, false)

        val comSoonLayout = binding.ComSoon
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("users").child(userId).child("role").get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.value as? String
                    if (role == "admin") {
                        // Делаем layout прозрачным и отключаем возможность взаимодействия
                        comSoonLayout.alpha = 0f   // Прозрачный (невидимый)
                        comSoonLayout.isClickable = false   // Отключаем клики
                        comSoonLayout.isFocusable = false   // Отключаем фокусировку
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Ошибка проверки роли: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Обработка команды через ввод текста
        binding.commandInput.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                val inputCommand = binding.commandInput.text.toString()
                handleCommand(inputCommand)
                true
            } else {
                false
            }
        }

        return binding.root
    }

    private fun handleCommand(command: String) {
        // Убираем лишние пробелы и разделяем команду на части
        val args = command.trim().split("\\s+".toRegex()).map { it.trim() }
        if (args.isEmpty() || args[0].isEmpty()) {
            displayOutput("Ошибка: Команда не распознана.")
            return
        }

        val userId = auth.currentUser?.uid ?: return displayOutput("Ошибка: Пользователь не авторизован.")

        when (args[0]) {
            "/setUsername" -> {
                if (args.size < 2) {
                    displayOutput("Ошибка: Укажите новый ник.")
                    return
                }
                val newUsername = args[1]
                database.getReference("users").child(userId).child("username").setValue(newUsername)
                    .addOnSuccessListener { displayOutput("Ваш никнейм изменен на $newUsername.") }
                    .addOnFailureListener { displayOutput("Ошибка изменения никнейма: ${it.message}") }
            }

            "/changeEmail" -> {
                if (args.size < 2) {
                    displayOutput("Ошибка: Укажите новый email.")
                    return
                }
                val newEmail = args[1]
                auth.currentUser?.updateEmail(newEmail)
                    ?.addOnSuccessListener { displayOutput("Ваш email изменен на $newEmail.") }
                    ?.addOnFailureListener { displayOutput("Ошибка изменения email: ${it.message}") }
            }

            "/changePassword" -> {
                if (args.size < 2) {
                    displayOutput("Ошибка: Укажите новый пароль.")
                    return
                }
                val newPassword = args[1]
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnSuccessListener { displayOutput("Ваш пароль успешно изменен.") }
                    ?.addOnFailureListener { displayOutput("Ошибка изменения пароля: ${it.message}") }
            }

            "/adminSetUsername" -> {
                if (args.size < 3) {
                    displayOutput("Ошибка: Укажите текущий ник и новый ник.")
                    return
                }
                val currentUsername = args[1]
                val newUsername = args[2]

                database.getReference("users").child(userId).child("role").get()
                    .addOnSuccessListener { roleSnapshot ->
                        val role = roleSnapshot.value as? String
                        if (role == "admin") {
                            database.getReference("users").orderByChild("username").equalTo(currentUsername)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.childrenCount == 0L) {
                                        displayOutput("Ошибка: Пользователь с ником $currentUsername не найден.")
                                    } else {
                                        for (child in snapshot.children) {
                                            child.ref.child("username").setValue(newUsername)
                                                .addOnSuccessListener {
                                                    displayOutput("Никнейм пользователя $currentUsername изменен на $newUsername.")
                                                }
                                                .addOnFailureListener { displayOutput("Ошибка изменения никнейма: ${it.message}") }
                                        }
                                    }
                                }
                                .addOnFailureListener { displayOutput("Ошибка доступа к базе данных: ${it.message}") }
                        } else {
                            displayOutput("Ошибка: У вас недостаточно прав для выполнения команды.")
                        }
                    }
                    .addOnFailureListener { displayOutput("Ошибка проверки роли: ${it.message}") }
            }

            // Удаление пользователя
            "/ban" -> {
                if (args.size < 2) {
                    displayOutput("Ошибка: Укажите никнейм для бана.")
                    return
                }
                val targetUsername = args[1]

                // Проверка на админский доступ
                database.getReference("users").child(userId).child("role").get()
                    .addOnSuccessListener { roleSnapshot ->
                        val role = roleSnapshot.value as? String
                        if (role == "admin") {
                            database.getReference("users").orderByChild("username").equalTo(targetUsername)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.childrenCount == 0L) {
                                        displayOutput("Ошибка: Пользователь с ником $targetUsername не найден.")
                                    } else {
                                        for (child in snapshot.children) {
                                            child.ref.removeValue()
                                                .addOnSuccessListener {
                                                    displayOutput("Пользователь $targetUsername заблокирован и удален.")
                                                }
                                                .addOnFailureListener { displayOutput("Ошибка удаления пользователя: ${it.message}") }
                                        }
                                    }
                                }
                                .addOnFailureListener { displayOutput("Ошибка доступа к базе данных: ${it.message}") }
                        } else {
                            displayOutput("Ошибка: У вас недостаточно прав для выполнения команды.")
                        }
                    }
                    .addOnFailureListener { displayOutput("Ошибка проверки роли: ${it.message}") }
            }

            // Удаление сборки из cardLibraries
            "/deleteConfig" -> {
                if (args.size < 3) {
                    displayOutput("Ошибка: Укажите ник и название сборки для удаления.")
                    return
                }
                val targetUsername = args[1]
                val configName = args[2]

                database.getReference("users").orderByChild("username").equalTo(targetUsername)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.childrenCount == 0L) {
                            displayOutput("Ошибка: Пользователь с ником $targetUsername не найден.")
                        } else {
                            for (child in snapshot.children) {
                                child.ref.child("cardLibraries").child(configName).removeValue()
                                    .addOnSuccessListener {
                                        displayOutput("Сборка $configName удалена для пользователя $targetUsername.")
                                    }
                                    .addOnFailureListener { displayOutput("Ошибка удаления сборки: ${it.message}") }
                            }
                        }
                    }
                    .addOnFailureListener { displayOutput("Ошибка доступа к базе данных: ${it.message}") }
            }

            "/copyUserData" -> {
                if (args.size < 3) {
                    displayOutput("Ошибка: Укажите UID источника и UID цели.")
                    return
                }
                val sourceUID = args[1]
                val targetUID = args[2]

                // Получаем данные пользователя-источника
                database.getReference("users").child(sourceUID).get()
                    .addOnSuccessListener { sourceSnapshot ->
                        if (!sourceSnapshot.exists()) {
                            displayOutput("Ошибка: Пользователь с UID $sourceUID не найден.")
                            return@addOnSuccessListener
                        }

                        // Копируем данные пользователя-источника
                        val sourceData = sourceSnapshot.value
                        database.getReference("users").child(targetUID).setValue(sourceData)
                            .addOnSuccessListener {
                                displayOutput("Данные пользователя $sourceUID успешно скопированы в $targetUID.")
                            }
                            .addOnFailureListener { exception ->
                                displayOutput("Ошибка при копировании данных: ${exception.message}")
                            }
                    }
                    .addOnFailureListener { exception ->
                        displayOutput("Ошибка доступа к данным источника: ${exception.message}")
                    }
            }

            // Удаление сборки из viewLibraries
            "/deleteView" -> {
                if (args.size < 3) {
                    displayOutput("Ошибка: Укажите ник и название сборки для удаления.")
                    return
                }
                val targetUsername = args[1]
                val viewName = args[2]

                database.getReference("users").orderByChild("username").equalTo(targetUsername)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.childrenCount == 0L) {
                            displayOutput("Ошибка: Пользователь с ником $targetUsername не найден.")
                        } else {
                            for (child in snapshot.children) {
                                child.ref.child("viewLibraries").child(viewName).removeValue()
                                    .addOnSuccessListener {
                                        displayOutput("Сборка $viewName удалена из viewLibraries для пользователя $targetUsername.")
                                    }
                                    .addOnFailureListener { displayOutput("Ошибка удаления сборки: ${it.message}") }
                            }
                        }
                    }
                    .addOnFailureListener { displayOutput("Ошибка доступа к базе данных: ${it.message}") }
            }

            else -> displayOutput("Ошибка: Неизвестная команда.")
        }

        binding.commandInput.text.clear()
    }


    private fun displayOutput(message: String) {
        binding.textView7.append("\n$message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
