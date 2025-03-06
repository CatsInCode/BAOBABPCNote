package com.example.bar

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Функция для добавления записи
    fun addRecordToCurrentUser(cardLibrary: CardLibrary, recordName: String, context: Context) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val recordData = cardLibrary.exportToDatabase()
            val userRef = database.getReference("users").child(userId).child("cardLibraries")

            // Добавляем запись в Firebase с названием и данными из cardLibrary
            val recordMap = mutableMapOf<String, Any>(
                recordName to recordData
            )

            Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()

            userRef.updateChildren(recordMap)
                .addOnSuccessListener {
                    // Логируем успешное добавление записи
                    println("Запись успешно добавлена для пользователя $userId")
                }
                .addOnFailureListener { e ->
                    // Логируем ошибку при добавлении записи
                    println("Ошибка при добавлении записи: ${e.message}")
                }
        } else {
            println("Ошибка: Пользователь не авторизован")
        }
    }

    // Функция для получения сборки по имени
    fun getRecordByName(recordName: String, callback: (Map<String, List<Component>>?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId).child("cardLibraries").child(recordName)

            userRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Получаем данные как Map<String, Any>
                        val recordData = snapshot.value as? Map<String, Any>
                        if (recordData != null) {
                            // Преобразуем данные в Map<String, List<Component>>
                            val componentsMap = mutableMapOf<String, List<Component>>()

                            for ((cardId, components) in recordData) {
                                if (components is List<*>) {
                                    val componentList = components.filterIsInstance<Map<String, Any>>().map { componentData ->
                                        // Создаем Component на основе данных из HashMap
                                        val name = componentData["name"] as? String ?: ""
                                        val link = componentData["link"] as? String ?: ""
                                        val price = componentData["price"] as? String ?: ""
                                        val type = ComponentType.valueOf(componentData["type"] as? String ?: "OTHER")
                                        val componentId = componentData["id"] as? String ?: ""

                                        Component(name, link, price, type, componentId)
                                    }
                                    componentsMap[cardId] = componentList
                                }
                            }

                            // Возвращаем данные в callback
                            callback(componentsMap)
                        } else {
                            println("Ошибка: Невозможно преобразовать данные из базы данных")
                            callback(null)
                        }
                    } else {
                        println("Ошибка: Запись с именем $recordName не найдена")
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    println("Ошибка при загрузке записи: ${e.message}")
                    callback(null)
                }
        } else {
            println("Ошибка: Пользователь не авторизован")
            callback(null)
        }
    }

    fun saveSelectedToViewLibraries(
        context: Context,
        cardUIElementsList: List<cardUIElements>,
        lib: CardLibrary
    ) {
        // Создание диалога для ввода имени сборки
        val dialogView = LayoutInflater.from(context).inflate(R.layout.name_enter_pub, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val nameInput = dialogView.findViewById<EditText>(R.id.CUSTOM_nameInput)
        val saveButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val switch1 = dialogView.findViewById<Switch>(R.id.switch1)  // Переключатель

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()

            if (name.isNotEmpty()) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val selectedData = mutableMapOf<String, Any>()
                    var name_: String = ""
                    var componentData = mapOf("name" to "name")

                    // Проверяем, если switch1 включен, сохраняем в публичные сборки
                    if (switch1.isChecked) {
                        checkIfPublicNameExists(name) { exists ->
                            if (exists) {
                                // Если имя уже занято, показываем сообщение и повторно запрашиваем имя
                                Toast.makeText(context, "Имя уже занято. Пожалуйста, введите другое имя.", Toast.LENGTH_SHORT).show()
                                return@checkIfPublicNameExists
                            } else {
                                // Сохраняем в публичные сборки
                                saveToPublicLibraries(userId, name, cardUIElementsList, lib, context, dialog)
                            }
                        }
                    } else {
                        // Если switch1 не включен, сохраняем в личные сборки
                        saveToPersonalViewLibraries(userId, name, cardUIElementsList, lib, context, dialog)
                    }

                } else {
                    Toast.makeText(context, "Ошибка: Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Функция для проверки занятости имени в публичных сборках
    private fun checkIfPublicNameExists(name: String, callback: (Boolean) -> Unit) {
        val publicRef = database.getReference("PUBLICK")
        publicRef.child(name).get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.exists()) // Если имя существует, возвращаем true
            }
    }

    // Функция для сохранения данных в публичные сборки
    private fun saveToPublicLibraries(
        userId: String,
        name: String,
        cardUIElementsList: List<cardUIElements>,
        lib: CardLibrary,
        context: Context,
        dialog: AlertDialog
    ) {
        val publicLibrariesRef = database.getReference("PUBLICK")
        val selectedData = mutableMapOf<String, Any>()
        var name_: String
        var componentData = mapOf("name" to "name")

        // Итерируем по списку CardUIElements
        for (cardUIElement in cardUIElementsList) {
            val selectedItem = cardUIElement.spinner.selectedItem
            if (selectedItem != null) {
                if (lib.getComponentTypesByCardId(cardUIElement.cardId) != ComponentType.OTHER) {
                    name_ = lib.getComponentTypesByCardId(cardUIElement.cardId)
                        ?.let { it1 -> getComponentNameByType(it1, context).toString() }
                        .toString()
                } else {
                    name_ = cardUIElement.cardId
                }

                val (link, price) = lib.getComponentLinkAndPriceByCardIdAndName(
                    cardUIElement.cardId,
                    selectedItem.toString()
                )

                // Преобразуем выбранный объект в Map
                componentData = mapOf(
                    "name" to selectedItem.toString(),
                    "link" to link.toString(),
                    "price" to price.toString()
                )

                // Сохраняем данные в публичную коллекцию
                publicLibrariesRef.child(name).child(name_).setValue(componentData)
                    .addOnSuccessListener {
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Ошибка сохранения в публичные сборки: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        Toast.makeText(context, "Сохранено в публичные", Toast.LENGTH_SHORT).show()
    }

    // Функция для сохранения данных в личные сборки
    private fun saveToPersonalViewLibraries(
        userId: String,
        name: String,
        cardUIElementsList: List<cardUIElements>,
        lib: CardLibrary,
        context: Context,
        dialog: AlertDialog
    ) {
        val viewLibrariesRef = database.getReference("users")
            .child(userId)
            .child("viewLibraries")

        val selectedData = mutableMapOf<String, Any>()
        var name_: String
        var componentData = mapOf("name" to "name")

        // Итерируем по списку CardUIElements
        for (cardUIElement in cardUIElementsList) {
            val selectedItem = cardUIElement.spinner.selectedItem
            if (selectedItem != null) {
                if (lib.getComponentTypesByCardId(cardUIElement.cardId) != ComponentType.OTHER) {
                    name_ = lib.getComponentTypesByCardId(cardUIElement.cardId)
                        ?.let { it1 -> getComponentNameByType(it1, context).toString() }
                        .toString()
                } else {
                    name_ = cardUIElement.cardId
                }

                val (link, price) = lib.getComponentLinkAndPriceByCardIdAndName(
                    cardUIElement.cardId,
                    selectedItem.toString()
                )

                // Преобразуем выбранный объект в Map
                componentData = mapOf(
                    "name" to selectedItem.toString(),
                    "link" to link.toString(),
                    "price" to price.toString()
                )

                // Сохраняем данные в личную коллекцию
                viewLibrariesRef.child(name).child(name_).setValue(componentData)
                    .addOnSuccessListener {
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Ошибка сохранения в личные сборки: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        Toast.makeText(context, "Сохранено в View Libraries", Toast.LENGTH_SHORT).show()
    }


    fun getComponentNameByType(type: ComponentType, context: Context): String? {
        return when (type) {
            ComponentType.MB -> context.getString(R.string.MB)
            ComponentType.CPU -> context.getString(R.string.CPU)
            ComponentType.GPU -> context.getString(R.string.GPU)
            ComponentType.RAM -> context.getString(R.string.RAM)
            ComponentType.COOL -> context.getString(R.string.COOL)
            ComponentType.DISK -> context.getString(R.string.DISK)
            ComponentType.CASE -> context.getString(R.string.CASE)
            ComponentType.BP -> context.getString(R.string.BP)
            ComponentType.OTHER -> null // Returns null for OTHER
        }
    }

    fun getAndCreateCardsByViewNamePUB(viewName: String, context: Context, parentLayout: ConstraintLayout) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Публичные сборки хранятся в разделе PUBLICK
        val publicRef = FirebaseDatabase.getInstance().getReference("PUBLICK").child(viewName)

        publicRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Очищаем родительский контейнер перед добавлением новых карточек
                    parentLayout.removeAllViews()

                    // Проходим по каждому элементу в публичных сборках и создаем карточки
                    var topMargin = 20.dpToPx(context) // Начальный отступ для первой карточки

                    snapshot.children.forEach { cardSnapshot ->
                        // Получаем данные карточки
                        val cardName = cardSnapshot.key ?: "Unknown"
                        val componentData = cardSnapshot.value as? Map<String, Any>

                        val name = componentData?.get("name") as? String ?: "Unknown"
                        val link = componentData?.get("link") as? String ?: "No link"
                        val price = componentData?.get("price") as? String ?: "0"

                        // Загружаем layout карточки из XML
                        val cardView = LayoutInflater.from(context).inflate(R.layout.visualizer_card, parentLayout, false) as ConstraintLayout

                        // Устанавливаем значения в карточку
                        val cardNameTextView = cardView.findViewById<TextView>(R.id.cardName)
                        val componentNameTextView = cardView.findViewById<TextView>(R.id.componentName)
                        val linkTextView = cardView.findViewById<TextView>(R.id.textView8)
                        val priceTextView = cardView.findViewById<TextView>(R.id.CUSTOM_costView)

                        cardNameTextView.text = cardName
                        componentNameTextView.text = name
                        linkTextView.text = link
                        priceTextView.text = "$price$"

                        // Устанавливаем отступы для каждой карточки
                        val layoutParams = cardView.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        layoutParams.topMargin = topMargin
                        cardView.layoutParams = layoutParams

                        // Добавляем карточку в родительский layout
                        parentLayout.addView(cardView)

                        // Обновляем отступ для следующей карточки
                        topMargin += 172.dpToPx(context) // Карточки имеют высоту 152dp и отступы 20dp
                    }
                } else {
                    Toast.makeText(context, "Ошибка: Запись с именем $viewName не найдена", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка при загрузке данных: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAndCreateCardsByViewName(viewName: String, context: Context, parentLayout: ConstraintLayout) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Получаем ссылку на viewLibraries в Firebase
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("viewLibraries").child(viewName)

            userRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Очищаем родительский контейнер перед добавлением новых карточек
                        parentLayout.removeAllViews()

                        // Проходим по каждому элементу в viewLibraries и создаем карточки
                        var topMargin = 20.dpToPx(context) // Начальный отступ для первой карточки

                        snapshot.children.forEach { cardSnapshot ->
                            // Получаем данные карточки
                            val cardName = cardSnapshot.key ?: "Unknown"
                            val componentData = cardSnapshot.value as? Map<String, Any>

                            val name = componentData?.get("name") as? String ?: "Unknown"
                            val link = componentData?.get("link") as? String ?: "No link"
                            val price = componentData?.get("price") as? String ?: "0"

                            // Загружаем layout карточки из XML
                            val cardView = LayoutInflater.from(context).inflate(R.layout.visualizer_card, parentLayout, false) as ConstraintLayout

                            // Устанавливаем значения в карточку
                            val cardNameTextView = cardView.findViewById<TextView>(R.id.cardName)
                            val componentNameTextView = cardView.findViewById<TextView>(R.id.componentName)
                            val linkTextView = cardView.findViewById<TextView>(R.id.textView8)
                            val priceTextView = cardView.findViewById<TextView>(R.id.CUSTOM_costView)

                            cardNameTextView.text = cardName
                            componentNameTextView.text = name
                            linkTextView.text = link
                            priceTextView.text = "$price$"

                            // Устанавливаем отступы для каждой карточки
                            val layoutParams = cardView.layoutParams as ConstraintLayout.LayoutParams
                            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                            layoutParams.topMargin = topMargin
                            cardView.layoutParams = layoutParams

                            // Добавляем карточку в родительский layout
                            parentLayout.addView(cardView)

                            // Обновляем отступ для следующей карточки
                            topMargin += 172.dpToPx(context) // Карточки имеют высоту 152dp и отступы 20dp
                        }
                    } else {
                        Toast.makeText(context, "Ошибка: Запись с именем $viewName не найдена", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Ошибка при загрузке данных: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Ошибка: Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

}