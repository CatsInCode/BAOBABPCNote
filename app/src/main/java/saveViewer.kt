package com.example.bar

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.bar.AuthResponse
import com.example.bar.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

object LibraryManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Функция для импорта всех сборок
    fun importLibrary(context: Context, cardLayout: ConstraintLayout, firstAnchorView: View, fragment: androidx.fragment.app.Fragment) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            var previousCardId: Int = firstAnchorView.id
            // Импорт данных из cardLibraries
            userRef.child("cardLibraries").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cardViews = mutableListOf<View>()

                    for (record in snapshot.children) {
                        val recordName = record.key
                        val recordData = record.value

                        if (recordName != null && recordData != null) {
                            val cardView = createCardView(context, cardLayout, recordName, recordData.toString())
                            cardLayout.addView(cardView)

                            // Создаем LayoutParams для привязки
                            val layoutParams = ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topToBottom = previousCardId
                                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                                topMargin = 20.dpToPx(context) // Отступ 20dp сверху
                            }

                            cardView.layoutParams = layoutParams
                            previousCardId = cardView.id // Обновляем привязку для следующей карточки

                            // Добавляем карточку в список
                            cardViews.add(cardView)
                        }
                    }

                    // При удалении карточки обновляем привязки
                    for (cardView in cardViews) {
                        val deleteButton = cardView.findViewById<ImageView>(R.id.imageView6)
                        deleteButton.setOnClickListener {
                            showDeleteConfirmationDialog(context, cardView, cardLayout)
                        }

                        val editButton = cardView.findViewById<ImageView>(R.id.imageViewR)
                        editButton.setOnClickListener {
                            val navController = findNavController(fragment)
                            val bundle = Bundle().apply {
                                putString("message", cardView.findViewById<EditText>(R.id.build).text.toString())
                            }
                            navController.navigate(R.id.nav_home, bundle)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Ошибка загрузки данных: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })

            // Импорт данных из viewLibraries
            userRef.child("viewLibraries").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val viewViews = mutableListOf<View>()

                    for (record in snapshot.children) {
                        val recordName = record.key
                        val recordData = record.value

                        if (recordName != null && recordData != null) {
                            val viewView = createViewView(context, cardLayout, recordName, recordData.toString())

                            // Генерируем уникальный идентификатор для viewView
                            viewView.id = View.generateViewId()

                            cardLayout.addView(viewView)

                            // Создаем LayoutParams для привязки
                            val layoutParams = ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topToBottom = previousCardId
                                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                                topMargin = 20.dpToPx(context) // Отступ 20dp сверху
                            }

                            viewView.layoutParams = layoutParams
                            previousCardId = viewView.id

                            // Добавляем view в список
                            viewViews.add(viewView)
                        }
                    }


                    // При удалении view обновляем привязки
                    for (viewView in viewViews) {
                        val deleteButton = viewView.findViewById<ImageView>(R.id.imageView6)
                        deleteButton.setOnClickListener {
                            showDeleteConfirmationDialog(context, viewView, cardLayout)
                        }

                        val editButton = viewView.findViewById<ImageView>(R.id.imageViewR)
                        editButton.setOnClickListener {
                            val navController = findNavController(fragment)
                            val bundle = Bundle().apply {
                                putString("message", viewView.findViewById<EditText>(R.id.build2).text.toString())
                            }
                            navController.navigate(R.id.nav_slideshow, bundle) // Переход в nav_slideshow
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Ошибка загрузки данных: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Toast.makeText(context, "Ошибка: Пользователь не авторизован", Toast.LENGTH_LONG).show()
        }
    }


    fun createViewView(context: Context, parentLayout: ConstraintLayout, recordName: String, recordData: String): View {
        // Создаем и настраиваем view для элемента из viewLibraries
        val viewView = LayoutInflater.from(context).inflate(R.layout.view_w_card, parentLayout, false)
        val viewNameTextView = viewView.findViewById<TextView>(R.id.build2)

        viewNameTextView.text = recordName // Заполняем только название

        // Дополнительная обработка данных, если необходимо
        // Можно использовать recordData, чтобы передать его через bundle или другое место

        return viewView
    }

    // Функция для создания карточки
    private fun createCardView(context: Context, cardLayout: ConstraintLayout, recordName: String, recordData: String): View {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.view_card, cardLayout, false)

        cardView.id = View.generateViewId()
        val editText = cardView.findViewById<EditText>(R.id.build)
        val deleteButton = cardView.findViewById<ImageView>(R.id.imageView6)

        // Устанавливаем имя сборки
        editText.setText(recordName)

        return cardView
    }

    // Функция для показа диалогового окна подтверждения
    private fun showDeleteConfirmationDialog(context: Context, cardView: View, cardLayout: ConstraintLayout) {
        // Инициализация диалога с кастомным дизайном
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.is_ok, null)

        // Создание диалога без стандартных кнопок
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)  // Делает диалог некликабельным в пустой области
            .create()

        // Находим кастомные кнопки
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Устанавливаем логику для кнопки "Да"
        confirmButton.setOnClickListener {
            // Удаляем карточку из layout
            cardLayout.removeView(cardView)
            // Обновляем привязки оставшихся карточек
            updateCardViewPositions(cardLayout)
            dialog.dismiss() // Закрываем диалог
        }

        // Устанавливаем логику для кнопки "Отмена"
        cancelButton.setOnClickListener {
            dialog.dismiss() // Просто закрываем диалог
        }

        dialog.show() // Показываем кастомный диалог
    }


    // Обновление привязок карточек
    private fun updateCardViewPositions(cardLayout: ConstraintLayout) {
        val allCardViews = mutableListOf<View>()

        // Собираем все карточки
        for (i in 0 until cardLayout.childCount) {
            allCardViews.add(cardLayout.getChildAt(i))
        }

        var previousCardId: Int = cardLayout.id // Начальная привязка (к родительскому контейнеру)

        // Для каждой карточки обновляем LayoutParams
        for (cardView in allCardViews) {
            val layoutParams = cardView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topToBottom = previousCardId
            layoutParams.topMargin = 20.dpToPx(cardLayout.context)
            cardView.layoutParams = layoutParams

            // Обновляем предыдущую карточку
            previousCardId = cardView.id
        }
    }

    // Удаление записи из Firebase
    private fun deleteRecord(recordName: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val recordRef = database.getReference("users").child(userId).child("cardLibraries").child(recordName)

            recordRef.removeValue()
                .addOnSuccessListener {
                    println("Запись '$recordName' успешно удалена.")
                }
                .addOnFailureListener { e ->
                    println("Ошибка при удалении записи: ${e.message}")
                }
        }
    }

    // Расширение для конвертации dp в px
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

interface ApiService {
    @POST("login")
    suspend fun login(@Body user: User): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body user: User): Response<AuthResponse>
}