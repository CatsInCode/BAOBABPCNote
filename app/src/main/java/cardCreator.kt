package com.example.bar

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout

object ComponentCardUtils {

    private val cardComponentsMap = mutableMapOf<String, MutableList<Component>>()

    // Получить список компонентов по cardId
    fun getComponentsByCardId(cardId: String): MutableList<Component>? {
        return cardComponentsMap[cardId]
    }

    // Функция для создания карточки компонента
    fun createComponentCard(
        addButton: Button,         // Кнопка для добавления нового компонента
        spinner: Spinner,          // Spinner для отображения списка компонентов
        priceTextView: TextView,   // TextView для отображения цены
        editButton: ImageView,     // Кнопка редактирования компонента
        componentType: ComponentType, // Тип компонента (например, MB, CPU)
        components: MutableList<Component>,  // Список всех компонентов
        spinnerAdapter: ArrayAdapter<String>,  // Адаптер для Spinner
        cardLibrary: CardLibrary,
        cardId: String
    ) {

        if (!cardComponentsMap.containsKey(cardId)) {
            cardComponentsMap[cardId] = components
        }

        // Обработчик кнопки "Добавить"
        addButton.setOnClickListener {
            showInputDialog(componentType, null, components, spinner, spinnerAdapter, priceTextView, cardLibrary, cardId)
        }

        // Обработчик изменения выбранного компонента в Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedName = parent.getItemAtPosition(position) as String
                val selectedComponent = components.find { it.name == selectedName }

                // Обновляем TextView с ценой
                selectedComponent?.let {
                    val priceText = "${it.price} $"
                    priceTextView.text = priceText
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                priceTextView.text = ""
            }
        }

        // Обработчик нажатия на кнопку редактирования
        editButton.setOnClickListener {
            if (spinner.adapter == null || spinner.adapter.count == 0) {
                Toast.makeText(spinner.context, "Список компонентов пуст", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPosition = spinner.selectedItemPosition
            if (selectedPosition >= 0) {
                val selectedComponent = components[selectedPosition]
                showInputDialog(componentType, selectedComponent, components, spinner, spinnerAdapter, priceTextView, cardLibrary, cardId)
            } else {
                Toast.makeText(spinner.context, "Выберите компонент для редактирования", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Метод для отображения диалога с вводом данных
    private fun showInputDialog(
        componentType: ComponentType,
        componentToEdit: Component?,
        components: MutableList<Component>,
        spinner: Spinner,
        spinnerAdapter: ArrayAdapter<String>,
        priceTextView: TextView,
        cardLibrary: CardLibrary,
        cardId: String
    ) {
        val dialogView = LayoutInflater.from(priceTextView.context).inflate(R.layout.dialog_input, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.CUSTOM_nameInput)
        val linkEditText: EditText = dialogView.findViewById(R.id.editTextLink)
        val priceEditText: EditText = dialogView.findViewById(R.id.editTextPrice)
        val saveTextView: TextView = dialogView.findViewById(R.id.confirmButton) // "Сохранить"
        val cancelTextView: TextView = dialogView.findViewById(R.id.cancelButton) // "Отмена"

        if (componentToEdit != null) {
            // Заполняем поля данными из компонента
            nameEditText.setText(componentToEdit.name)
            linkEditText.setText(componentToEdit.link)
            priceEditText.setText(componentToEdit.price)
        }

        // Создаем диалог с кастомным стилем
        val dialog = AlertDialog.Builder(priceTextView.context, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        // Обработчик кнопки "Сохранить"
        saveTextView.setOnClickListener {
            val name = nameEditText.text.toString()
            val link = linkEditText.text.toString()
            val price = priceEditText.text.toString()

            if (name.isNotEmpty() && link.isNotEmpty() && price.isNotEmpty()) {
                if (componentToEdit != null) {
                    // Удаляем старый компонент
                    components.remove(componentToEdit)
                    spinnerAdapter.remove(componentToEdit.name)

                    // Обновляем данные компонента
                    componentToEdit.name = name
                    componentToEdit.link = link
                    componentToEdit.price = price

                    // Добавляем обновленный компонент в список и адаптер
                    components.add(componentToEdit)
                    spinnerAdapter.add(componentToEdit.name)

                } else {
                    // Создаем новый компонент
                    val component1Id = cardLibrary.addComponentToCard(
                        cardId,
                        name = name,
                        link = link,
                        price = price,
                        type = componentType
                    )
                    val newComponent = Component(name, link, price, componentType, component1Id)
                    components.add(newComponent)
                    spinnerAdapter.add(newComponent.name)

                }

                // Обновляем адаптер
                spinnerAdapter.notifyDataSetChanged()

                // Найдем индекс компонента в списке
                val position = components.indexOf(componentToEdit ?: components.last())

                // Устанавливаем новый элемент в Spinner
                spinner.setSelection(position)

                // Обновляем цену в priceTextView после сохранения изменений
                val updatedComponent = componentToEdit ?: components.last()
                val updatedPriceText = "${updatedComponent.price} $"
                priceTextView.text = updatedPriceText

                dialog.dismiss() // Закрыть диалог
            } else {
                Toast.makeText(priceTextView.context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработчик кнопки "Отмена"
        cancelTextView.setOnClickListener {
            dialog.dismiss() // Закрыть диалог
        }

        dialog.show()
    }

    fun saveComponent(
        componentType: ComponentType,
        componentToEdit: Component?,
        components: MutableList<Component>?,
        spinner: Spinner?,
        spinnerAdapter: ArrayAdapter<String>?,
        priceTextView: TextView?,
        cardLibrary: CardLibrary,
        cardId: String,
        name: String?,
        link: String?,
        price: String?
    ) {
        // Проверяем, что все параметры (name, link, price) не пустые
        if (!name.isNullOrEmpty() && !link.isNullOrEmpty() && !price.isNullOrEmpty()) {
            if (componentToEdit != null) {
                // Удаляем старый компонент
                if (components != null) {
                    components.remove(componentToEdit)
                }
                spinnerAdapter?.remove(componentToEdit.name)

                // Обновляем данные компонента
                componentToEdit.name = name
                componentToEdit.link = link
                componentToEdit.price = price

                // Добавляем обновленный компонент в список и адаптер
                if (components != null) {
                    components.add(componentToEdit)
                }
                spinnerAdapter?.add(componentToEdit.name)

                Toast.makeText(priceTextView?.context, "Запись обновлена", Toast.LENGTH_SHORT).show()
            } else {
                // Создаем новый компонент
                Log.v("ADD_PHYSICAL", "______________________________ADD_PHYSICAL________________________________")
                val component1Id = cardLibrary.addComponentToCard(
                    cardId,
                    name = name,
                    link = link,
                    price = price,
                    type = componentType
                )
                val newComponent = Component(name, link, price, componentType, component1Id)
                if (components != null) {
                    components.add(newComponent)
                }
                spinnerAdapter?.add(newComponent.name)

            }

            // Обновляем адаптер
            spinnerAdapter?.notifyDataSetChanged()


            // Обновляем цену в priceTextView после сохранения изменений
            val updatedComponent = componentToEdit ?: components?.last()
            val updatedPriceText = "${updatedComponent?.price} $"
            priceTextView?.text = updatedPriceText
        } else {
            Toast.makeText(priceTextView?.context, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }




    fun createAdapter(context: Context, spinner: Spinner): ArrayAdapter<String> {
        // Создаем адаптер с кастомным макетом
        val adapter = ArrayAdapter<String>(context, R.layout.colored_spinner, mutableListOf())

        // Устанавливаем кастомный макет для выпадающего списка
        adapter.setDropDownViewResource(R.layout.colored_spinner_dropdown)

        // Применяем адаптер к Spinner (всегда создаем новый адаптер для каждого Spinner)
        spinner.adapter = adapter

        return adapter
    }

    /*fun createAdapter(context: Context, spinner: Spinner): ArrayAdapter<String> {
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, mutableListOf())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        return adapter
    }*/

    data class ComponentCardElements(
        val addButton: Button,
        val spinner: Spinner,
        val costView: TextView,
        val editView: ImageView
    )

    fun addCard(
        context: Context,
        parentLayout: ConstraintLayout,
        previousViewId: Int,
        bp: Int,
        cardId: Int,
        sbButton: LinearLayout,
        cardLibrary: CardLibrary
    ): cardUIElements? { // Изменили тип возвращаемого значения на cardUIElements
        // Инфлейт кастомного диалогового окна
        val dialogView = LayoutInflater.from(context).inflate(R.layout.name_enter, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.CUSTOM_nameInput)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Переменная для хранения результата
        var result: cardUIElements? = null

        // Создаём и отображаем диалог
        val dialog = AlertDialog.Builder(context,  R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        // Обработчик кнопки Cancel
        cancelButton.setOnClickListener {
            dialog.dismiss() // Просто закрываем диалог
        }

        // Обработчик кнопки OK
        confirmButton.setOnClickListener {
            val cardName = nameInput.text.toString().ifBlank { "Default Name" }

            // Инфлейт XML компонента
            val inflater = LayoutInflater.from(context)
            val cardView = inflater.inflate(R.layout.component_card, parentLayout, false) as ConstraintLayout

            // Устанавливаем уникальный ID для новой карточки
            cardView.id = cardId

            // Привязка новой карточки к предыдущей
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = bp
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin =  (if (previousViewId > 0) (20 + (170 * previousViewId)) else 20).dpToPx(context) // 20dp отступ сверху
            }
            cardView.layoutParams = layoutParams

            // Добавляем карточку в родительский layout
            parentLayout.addView(cardView)

            // Устанавливаем имя карточки
            val cardNameView = cardView.findViewById<TextView>(R.id.CUSTOM_cardName)
            cardNameView.text = cardName

            // Привязка кнопки SB к низу новой карточки
            val sbLayoutParams = sbButton.layoutParams as ConstraintLayout.LayoutParams
            sbLayoutParams.topToBottom = cardId
            sbLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            sbLayoutParams.topMargin = 20.dpToPx(context) // 20dp отступ сверху
            sbButton.layoutParams = sbLayoutParams

            // Находим нужные элементы внутри карточки
            val addButton = cardView.findViewById<Button>(R.id.CUSTOM_addButton)
            val spinner = cardView.findViewById<Spinner>(R.id.CUSTOM_spinnerVariant)
            val costView = cardView.findViewById<TextView>(R.id.CUSTOM_costView)
            val editView = cardView.findViewById<ImageView>(R.id.CUSTOM_editView)

            // Сохраняем результат
            result = cardUIElements(
                cardId = cardId.toString(), // Преобразуем ID в строку
                addButton = addButton,
                spinner = spinner,
                priceTextView = costView,
                editButton = editView
            )

            // Закрываем диалог
            dialog.dismiss()

            // Добавляем компонент в cardLibrary с новым компонентом
            val cardId_ = cardLibrary.addCard(cardName)
            ComponentCardUtils.createComponentCard(
                addButton, // Кнопка добавления
                spinner,   // Spinner
                costView,  // TextView для отображения цены
                editView,  // Кнопка редактирования
                ComponentType.OTHER, // Тип компонента
                mutableListOf<Component>(), // Список компонентов
                ComponentCardUtils.createAdapter(context, spinner), // Адаптер Spinner
                cardLibrary,
                cardId_
            )
        }

        // Возвращаем результат
        return result
    }


    fun addCard_USENAME(
        context: Context,
        parentLayout: ConstraintLayout,
        previousViewId: Int,
        bp: Int,
        cardId: Int,
        sbButton: LinearLayout,
        cardLibrary: CardLibrary,
        cardName: String
    ): cardUIElements? {
        // Инфлейт кастомного диалогового окна
        val dialogView = LayoutInflater.from(context).inflate(R.layout.name_enter, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.CUSTOM_nameInput)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Переменная для хранения результата
        var result: cardUIElements? = null


        // Инфлейт XML компонента
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.component_card, parentLayout, false) as ConstraintLayout

        // Устанавливаем уникальный ID для новой карточки
        cardView.id = cardId

        // Привязка новой карточки к предыдущей
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = bp
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topMargin =  (if (previousViewId > 0) (20 + (170 * previousViewId)) else 20).dpToPx(context) // 20dp отступ сверху
        }
        cardView.layoutParams = layoutParams

        // Добавляем карточку в родительский layout
        parentLayout.addView(cardView)

        // Устанавливаем имя карточки
        val cardNameView = cardView.findViewById<TextView>(R.id.CUSTOM_cardName)
        cardNameView.text = cardName

        // Привязка кнопки SB к низу новой карточки
        val sbLayoutParams = sbButton.layoutParams as ConstraintLayout.LayoutParams
        sbLayoutParams.topToBottom = cardId
        sbLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        sbLayoutParams.topMargin = 20.dpToPx(context) // 20dp отступ сверху
        sbButton.layoutParams = sbLayoutParams

        // Находим нужные элементы внутри карточки
        val addButton = cardView.findViewById<Button>(R.id.CUSTOM_addButton)
        val spinner = cardView.findViewById<Spinner>(R.id.CUSTOM_spinnerVariant)
        val costView = cardView.findViewById<TextView>(R.id.CUSTOM_costView)
        val editView = cardView.findViewById<ImageView>(R.id.CUSTOM_editView)

        // Сохраняем результат


        val cardId_ = cardLibrary.addCard(cardName)
        ComponentCardUtils.createComponentCard(
            addButton, // Кнопка добавления
            spinner,         // Spinner
            costView, // TextView для отображения цены
            editView, // Кнопка редактирования
            ComponentType.OTHER, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            ComponentCardUtils.createAdapter(context, spinner),    // Адаптер Spinner
            cardLibrary,
            cardId_
        )

        result = cardUIElements(cardId_, addButton, spinner, costView, editView)

        // Возвращаем результат
        return result
    }


    // Функция для преобразования dp в px
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }


}