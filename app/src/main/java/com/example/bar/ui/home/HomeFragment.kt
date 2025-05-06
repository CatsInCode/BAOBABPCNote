package com.example.bar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bar.Component
import com.example.bar.ComponentCardUtils
import com.example.bar.FirebaseManager
import com.example.bar.ComponentType
import com.example.bar.databinding.FragmentHomeBinding
import com.example.bar.CardLibrary
import com.example.bar.ComponentCardUtils.ComponentCardElements
import com.example.bar.cardUIElements
import com.example.bar.ComponentCardUtils.getComponentsByCardId
import com.example.bar.ComponentCardUtils.saveComponent
import com.example.bar.R


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cardUIElementsList = mutableListOf<cardUIElements>()

    // Список компонентов и адаптер для Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val cardLibrary = CardLibrary()
        var cardId = cardLibrary.addCard()
        val addButton = binding.SB
        val expButton = binding.export
        val cardLayout = binding.cardLayout
        var cardCounter = 0
        var recLoadName:String = ""
        val baseCardIdList = mutableListOf<String>()
        addButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.nav_slideshow)
            val name = binding.buildName.text.toString()
            if (!name.isBlank()) {
                // Пример использования
                val recordName = name  // Имя записи
                // Добавляем запись в текущего пользователя
                cardLibrary.logCardLibrary()
                FirebaseManager.addRecordToCurrentUser(cardLibrary, recordName, requireContext())
            }
            else {
                Toast.makeText(requireContext(), "Введите название сборки!", Toast.LENGTH_SHORT).show()
            }
        }

        expButton.setOnClickListener {
                FirebaseManager.saveSelectedToViewLibraries(requireContext(), cardUIElementsList, cardLibrary)
        }

        fun addCardToStorage(
            cardId: String,
            addButton: Button,
            spinner: Spinner,
            priceTextView: TextView,
            editButton: ImageView,
            adapter: SpinnerAdapter
        ) {
            // Создаем объект CardUIElements с переданными параметрами
            val cardUI = cardUIElements(
                cardId = cardId,
                addButton = addButton,
                spinner = spinner,
                priceTextView = priceTextView,
                editButton = editButton
            )

            // Добавляем объект в список cardUIElementsList
            cardUIElementsList.add(cardUI)

            // Устанавливаем адаптер для спиннера
            spinner.adapter = adapter
        }

        val message = arguments?.getString("message")

        // Отображение сообщения в Toast, если оно не пустое
        message?.let {
            recLoadName = it
            binding.buildName.setText(recLoadName)
        }
        cardCounter = 0
        // Инициализируем Spinner

        // Создаем компонентную карточку
        val adapter = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.button2, binding.processorSpinner, binding.textView4, binding.imageView2, adapter)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.button2, // Кнопка добавления
                binding.processorSpinner,         // Spinner
                binding.textView4, // TextView для отображения цены
                binding.imageView2, // Кнопка редактирования
            ),
            ComponentType.MB, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapter,   // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterP = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.PButton2, binding.PProcessorSpinner, binding.PTextView4, binding.PImageView2, adapterP)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.PButton2, // Кнопка добавления
                binding.PProcessorSpinner,         // Spinner
                binding.PTextView4, // TextView для отображения цены
                binding.PImageView2
            ), // Кнопка редактирования
            ComponentType.CPU, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterP,   // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterG = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.GButton2, binding.GProcessorSpinner, binding.GTextView4, binding.GImageView2, adapterG)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.GButton2, // Кнопка добавления
                binding.GProcessorSpinner,         // Spinner
                binding.GTextView4, // TextView для отображения цены
                binding.GImageView2 // Кнопка редактирования
            ),
            ComponentType.GPU, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterG,    // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterR = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.RButton2, binding.RProcessorSpinner, binding.RTextView4, binding.RImageView2, adapterR)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.RButton2, // Кнопка добавления
                binding.RProcessorSpinner,         // Spinner
                binding.RTextView4, // TextView для отображения цены
                binding.RImageView2 // Кнопка редактирования
            ),
            ComponentType.RAM, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterR,    // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterF = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.FButton2, binding.FProcessorSpinner, binding.FTextView4, binding.FImageView2, adapterF)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.FButton2, // Кнопка добавления
                binding.FProcessorSpinner,         // Spinner
                binding.FTextView4, // TextView для отображения цены
                binding.FImageView2 // Кнопка редактирования
            ),
            ComponentType.COOL, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterF,    // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterS = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.SButton2, binding.SProcessorSpinner, binding.STextView4, binding.SImageView2, adapterS)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.SButton2, // Кнопка добавления
                binding.SProcessorSpinner,         // Spinner
                binding.STextView4, // TextView для отображения цены
                binding.SImageView2 // Кнопка редактирования
            ),
            ComponentType.DISK, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterS,    // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterCAS = ComponentCardUtils.createAdapter(requireContext(), binding.CASProcessorSpinner)
        addCardToStorage(cardId, binding.CASButton2, binding.CASProcessorSpinner, binding.CASTextView4, binding.CASImageView2, adapterCAS)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.CASButton2, // Кнопка добавления
                binding.CASProcessorSpinner,         // Spinner
                binding.CASTextView4, // TextView для отображения цены
                binding.CASImageView2, // Кнопка редактирования)
            ),
            ComponentType.CASE, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterCAS,    // Адаптер Spinner
            cardLibrary,
            cardId
        )

        cardId = cardLibrary.addCard()
        val adapterB = ComponentCardUtils.createAdapter(requireContext(), binding.BPProcessorSpinner)
        addCardToStorage(cardId, binding.BPButton2, binding.BPProcessorSpinner, binding.BPTextView4, binding.BPImageView2, adapterB)
        baseCardIdList.add(cardId);
        ComponentCardUtils.createComponentCard(
            ComponentCardElements(
                binding.BPButton2, // Кнопка добавления
                binding.BPProcessorSpinner,         // Spinner
                binding.BPTextView4, // TextView для отображения цены
                binding.BPImageView2, // Кнопка редактирования
            ),
            ComponentType.BP, // Тип компонента
            mutableListOf<Component>(),       // Список компонентов
            adapterB,    // Адаптер Spinner
            cardLibrary,
            cardId
        )



        fun addCard() {
            //Добавление карточки
            val newCardElements = ComponentCardUtils.addCard(
                context = requireContext(),
                parentLayout = cardLayout,
                previousViewId = cardCounter,
                binding.BP.id,
                cardId = View.generateViewId(), // Генерация уникального ID для карточки
                binding.BUTTLAY,
                cardLibrary,

            )
            if (newCardElements != null) {
                cardUIElementsList.add(newCardElements)
            }

            // Обновляем ID предыдущей карточки
            cardCounter++
        }

        fun autoAddCard(name:String): cardUIElements? {
            //Добавление карточки
            val newCardElements = ComponentCardUtils.addCard_USENAME(
                context = requireContext(),
                parentLayout = cardLayout,
                previousViewId = cardCounter,
                binding.BP.id,
                cardId = View.generateViewId(), // Генерация уникального ID для карточки
                binding.BUTTLAY,
                cardLibrary,
                name
            )

            // Обновляем ID предыдущей карточки
            cardCounter++
            return newCardElements
        }

        fun importAndDistributeComponents(
            cardLibrary: CardLibrary,
            importedData: Map<String, List<Component>>,
            cardIdList: List<String>,
        ) {
            // Проходим по всем записям в базе
            for ((cardId, components) in importedData) {
                for (component in components) {
                    val targetCardIndex = when (component.type) {
                        ComponentType.MB -> 0
                        ComponentType.CPU -> 1
                        ComponentType.GPU -> 2
                        ComponentType.RAM -> 3
                        ComponentType.COOL -> 4
                        ComponentType.DISK -> 5
                        ComponentType.CASE -> 6
                        ComponentType.BP -> 7
                        ComponentType.OTHER -> null
                    }

                    if (targetCardIndex != null) {
                        // Проверяем, существует ли карточка для компонента
                        if (targetCardIndex < cardIdList.size) {
                            val targetCardId = cardIdList[targetCardIndex]

                            // Добавляем компонент в соответствующую карточку

                            val foundCard = cardUIElementsList.find { it.cardId == targetCardId}

                            if (foundCard != null) {
                                saveComponent(
                                    component.type,
                                    null,
                                    getComponentsByCardId(targetCardId),
                                    foundCard.spinner,
                                    getArrayAdapterFromSpinner(foundCard.spinner),
                                    foundCard.priceTextView,
                                    cardLibrary,
                                    targetCardId,
                                    component.name,
                                    component.link,
                                    price = component.price
                                )
                            }
                        } else {
                            println("Ошибка: Номер карточки $targetCardIndex выходит за пределы списка cardIdList")
                        }
                    } else if (component.type == ComponentType.OTHER) {
                        // Если тип OTHER, создаём новую карточку
                        val foundCard = cardUIElementsList.find { it.cardId == cardId }

                        if (foundCard == null) {
                            // Если карточка с таким cardId не найдена, создаём новую
                            val newCardId = cardLibrary.addCard()
                            autoAddCard(cardId)?.let { cardUIElementsList.add(it) }
                        }

                        // Проверяем ещё раз, так как карточка могла быть добавлена
                        if (foundCard != null) {
                            saveComponent(
                                component.type,
                                null,
                                getComponentsByCardId(cardId),
                                foundCard.spinner,
                                getArrayAdapterFromSpinner(foundCard.spinner),
                                foundCard.priceTextView,
                                cardLibrary,
                                cardId,
                                component.name,
                                component.link,
                                price = component.price
                            )
                        }
                    }
                }
            }
        }

        binding.addComp.setOnClickListener {
            addCard()
        }
        if (!recLoadName.isBlank()) {
            FirebaseManager.getRecordByName(recLoadName) { importedData ->
                if (importedData != null) {
                    cardLibrary.importFromDatabase(importedData, baseCardIdList)

                    // После импорта, можно передать данные в importAndDistributeComponents
                    importAndDistributeComponents(
                        cardLibrary = cardLibrary,
                        importedData = importedData,
                        cardIdList = baseCardIdList,
                    )

                } else {
                    println("Ошибка при получении сборки.")
                }
            }
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun getArrayAdapterFromSpinner(spinner: Spinner): ArrayAdapter<String>? {
        val adapter = spinner.adapter
        return if (adapter is ArrayAdapter<*>) {
            // Приводим адаптер к ArrayAdapter<String> безопасно
            @Suppress("UNCHECKED_CAST")
            adapter as ArrayAdapter<String>
        } else {
            // Если адаптер не является ArrayAdapter<String>, возвращаем null
            null
        }
    }

    fun collectActiveElements(): Map<String, String> {
        val activeElements = mutableMapOf<String, String>()

        for (cardUI in cardUIElementsList) {
            // Получаем выбранный элемент из Spinner
            val selectedItem = cardUI.spinner.selectedItem?.toString()

            // Если элемент выбран, добавляем его в результат
            if (!selectedItem.isNullOrBlank()) {
                activeElements[cardUI.cardId] = selectedItem
            } else {
                // Если ничего не выбрано, можно задать значение по умолчанию
                activeElements[cardUI.cardId] = "Не выбрано"
            }
        }

        return activeElements
    }


}
