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
    fun getComponentsByCardId(cardId: String): MutableList<Component>? = cardComponentsMap[cardId]

    // Функция для создания карточки компонента
    fun createComponentCard(
        elements: ComponentCardElements,
        componentType: ComponentType,
        components: MutableList<Component>,
        spinnerAdapter: ArrayAdapter<String>,
        cardLibrary: CardLibrary,
        cardId: String
    ) {
        if (!cardComponentsMap.containsKey(cardId)) {
            cardComponentsMap[cardId] = components
        }

        setupAddButton(elements, componentType, components, spinnerAdapter, cardLibrary, cardId)
        setupSpinner(elements, components)
        setupEditButton(elements, componentType, components, spinnerAdapter, cardLibrary, cardId)
    }

    private fun setupAddButton(
        elements: ComponentCardElements,
        componentType: ComponentType,
        components: MutableList<Component>,
        spinnerAdapter: ArrayAdapter<String>,
        cardLibrary: CardLibrary,
        cardId: String
    ) {
        elements.addButton.setOnClickListener {
            showInputDialog(
                componentType = componentType,
                componentToEdit = null,
                components = components,
                spinner = elements.spinner,
                spinnerAdapter = spinnerAdapter,
                priceTextView = elements.priceTextView,
                cardLibrary = cardLibrary,
                cardId = cardId
            )
        }
    }

    private fun setupSpinner(
        elements: ComponentCardElements,
        components: MutableList<Component>
    ) {
        elements.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedName = parent.getItemAtPosition(position) as String
                components.find { it.name == selectedName }?.let {
                    elements.priceTextView.text = "${it.price} $"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                elements.priceTextView.text = ""
            }
        }
    }

    private fun setupEditButton(
        elements: ComponentCardElements,
        componentType: ComponentType,
        components: MutableList<Component>,
        spinnerAdapter: ArrayAdapter<String>,
        cardLibrary: CardLibrary,
        cardId: String
    ) {
        elements.editButton.setOnClickListener {
            if (elements.spinner.adapter == null || elements.spinner.adapter.count == 0) {
                Toast.makeText(elements.spinner.context, "Список компонентов пуст", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPosition = elements.spinner.selectedItemPosition
            if (selectedPosition >= 0) {
                val selectedComponent = components[selectedPosition]
                showInputDialog(
                    componentType = componentType,
                    componentToEdit = selectedComponent,
                    components = components,
                    spinner = elements.spinner,
                    spinnerAdapter = spinnerAdapter,
                    priceTextView = elements.priceTextView,
                    cardLibrary = cardLibrary,
                    cardId = cardId
                )
            } else {
                Toast.makeText(elements.spinner.context, "Выберите компонент для редактирования", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        val context = priceTextView.context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.CUSTOM_nameInput)
        val linkEditText = dialogView.findViewById<EditText>(R.id.editTextLink)
        val priceEditText = dialogView.findViewById<EditText>(R.id.editTextPrice)
        val saveTextView = dialogView.findViewById<TextView>(R.id.confirmButton)
        val cancelTextView = dialogView.findViewById<TextView>(R.id.cancelButton)

        componentToEdit?.let {
            nameEditText.setText(it.name)
            linkEditText.setText(it.link)
            priceEditText.setText(it.price)
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        saveTextView.setOnClickListener {
            val name = nameEditText.text.toString()
            val link = linkEditText.text.toString()
            val price = priceEditText.text.toString()

            if (name.isNotEmpty() && link.isNotEmpty() && price.isNotEmpty()) {
                saveComponent(
                    componentType = componentType,
                    componentToEdit = componentToEdit,
                    components = components,
                    spinner = spinner,
                    spinnerAdapter = spinnerAdapter,
                    priceTextView = priceTextView,
                    cardLibrary = cardLibrary,
                    cardId = cardId,
                    name = name,
                    link = link,
                    price = price
                )
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        cancelTextView.setOnClickListener { dialog.dismiss() }
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
        if (name.isNullOrEmpty() || link.isNullOrEmpty() || price.isNullOrEmpty()) {
            Toast.makeText(priceTextView?.context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (componentToEdit != null) {
            updateExistingComponent(componentToEdit, name, link, price, components, spinnerAdapter)
        } else {
            addNewComponent(componentType, name, link, price, components, spinnerAdapter, cardLibrary, cardId)
        }

        spinnerAdapter?.notifyDataSetChanged()
        priceTextView?.text = "${componentToEdit?.price ?: components?.lastOrNull()?.price} $"
    }

    private fun updateExistingComponent(
        component: Component,
        name: String,
        link: String,
        price: String,
        components: MutableList<Component>?,
        spinnerAdapter: ArrayAdapter<String>?
    ) {
        components?.remove(component)
        spinnerAdapter?.remove(component.name)

        component.name = name
        component.link = link
        component.price = price

        components?.add(component)
        spinnerAdapter?.add(component.name)
    }

    private fun addNewComponent(
        componentType: ComponentType,
        name: String,
        link: String,
        price: String,
        components: MutableList<Component>?,
        spinnerAdapter: ArrayAdapter<String>?,
        cardLibrary: CardLibrary,
        cardId: String
    ) {
        Log.v("ADD_PHYSICAL", "______________________________ADD_PHYSICAL________________________________")
        val componentId = cardLibrary.addComponentToCard(
            cardId,
            name = name,
            link = link,
            price = price,
            type = componentType
        )
        val newComponent = Component(name, link, price, componentType, componentId)
        components?.add(newComponent)
        spinnerAdapter?.add(newComponent.name)
    }

    fun createAdapter(context: Context, spinner: Spinner): ArrayAdapter<String> {
        val adapter = ArrayAdapter<String>(context, R.layout.colored_spinner, mutableListOf())
        adapter.setDropDownViewResource(R.layout.colored_spinner_dropdown)
        spinner.adapter = adapter
        return adapter
    }

    data class ComponentCardElements(
        val addButton: Button,
        val spinner: Spinner,
        val priceTextView: TextView,
        val editButton: ImageView
    )

    fun addCard(
        context: Context,
        parentLayout: ConstraintLayout,
        previousViewId: Int,
        bp: Int,
        cardId: Int,
        sbButton: LinearLayout,
        cardLibrary: CardLibrary
    ): cardUIElements? {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.name_enter, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.CUSTOM_nameInput)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        var result: cardUIElements? = null
        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        cancelButton.setOnClickListener { dialog.dismiss() }

        confirmButton.setOnClickListener {
            val cardName = nameInput.text.toString().ifBlank { "Default Name" }
            result = createCardView(
                context = context,
                parentLayout = parentLayout,
                previousViewId = previousViewId,
                bp = bp,
                cardId = cardId,
                sbButton = sbButton,
                cardLibrary = cardLibrary,
                cardName = cardName
            )
            dialog.dismiss()
        }

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
        return createCardView(
            context = context,
            parentLayout = parentLayout,
            previousViewId = previousViewId,
            bp = bp,
            cardId = cardId,
            sbButton = sbButton,
            cardLibrary = cardLibrary,
            cardName = cardName
        )
    }

    private fun createCardView(
        context: Context,
        parentLayout: ConstraintLayout,
        previousViewId: Int,
        bp: Int,
        cardId: Int,
        sbButton: LinearLayout,
        cardLibrary: CardLibrary,
        cardName: String
    ): cardUIElements {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.component_card, parentLayout, false) as ConstraintLayout
        cardView.id = cardId

        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = bp
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topMargin = (if (previousViewId > 0) (20 + (170 * previousViewId)) else 20).dpToPx(context)
        }
        cardView.layoutParams = layoutParams
        parentLayout.addView(cardView)

        cardView.findViewById<TextView>(R.id.CUSTOM_cardName).text = cardName

        val sbLayoutParams = sbButton.layoutParams as ConstraintLayout.LayoutParams
        sbLayoutParams.topToBottom = cardId
        sbLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        sbLayoutParams.topMargin = 20.dpToPx(context)
        sbButton.layoutParams = sbLayoutParams

        val addButton = cardView.findViewById<Button>(R.id.CUSTOM_addButton)
        val spinner = cardView.findViewById<Spinner>(R.id.CUSTOM_spinnerVariant)
        val costView = cardView.findViewById<TextView>(R.id.CUSTOM_costView)
        val editView = cardView.findViewById<ImageView>(R.id.CUSTOM_editView)

        val cardId_ = cardLibrary.addCard(cardName)
        createComponentCard(
            elements = ComponentCardElements(addButton, spinner, costView, editView),
            componentType = ComponentType.OTHER,
            components = mutableListOf(),
            spinnerAdapter = createAdapter(context, spinner),
            cardLibrary = cardLibrary,
            cardId = cardId_
        )

        return cardUIElements(cardId_, addButton, spinner, costView, editView)
    }

    fun Int.dpToPx(context: Context): Int = 
        (this * context.resources.displayMetrics.density).toInt()
}

data class cardUIElements(
    val cardId: String,
    val addButton: Button,
    val spinner: Spinner,
    val priceTextView: TextView,
    val editButton: ImageView
)
