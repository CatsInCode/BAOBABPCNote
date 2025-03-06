package com.example.bar

import android.util.Log
import java.util.*

class CardLibrary {
    private val cards = mutableMapOf<String, MutableList<Component>>()

    // Получить свободный ID для карты
    fun getFreeCardId(): String {
        return UUID.randomUUID().toString()
    }

    // Добавить новую карту
    fun addCard(customCardId: String? = null): String {
        val cardId = customCardId ?: getFreeCardId() // Используем переданный ID, если он есть, иначе генерируем новый
        cards[cardId] = mutableListOf()
        Log.v("CARDADD", cardId)
        return cardId
    }

    // Получить свободный ID для записи
    fun getFreeComponentId(cardId: String): String {
        Log.v("ADDCOMPONENT", "-> cardId: $cardId")

        // Логируем все содержимое карт
        Log.d("ADDCOMPONENT", "Current cards in library:")
        cards.forEach { (key, value) ->
            Log.d("ADDCOMPONENT", "Card ID: $key -> Components: $value")
        }

        // Проверяем, существует ли карта с таким ID
        if (!cards.containsKey(cardId)) throw IllegalArgumentException("Card with ID $cardId does not exist")

        return UUID.randomUUID().toString()
    }

    // Добавить запись в карту
    fun addComponentToCard(cardId: String, name: String, link: String, price: String, type: ComponentType): String {
        val componentId = getFreeComponentId(cardId)
        val component = Component(name, link, price, type, componentId)
        cards[cardId]?.add(component) ?: throw IllegalArgumentException("Card with ID $cardId does not exist")
        return componentId
    }

    // Удалить запись из карты
    fun removeComponent(cardId: String, componentId: String): Boolean {
        val cardComponents = cards[cardId] ?: throw IllegalArgumentException("Card with ID $cardId does not exist")
        return cardComponents.removeIf { it.id == componentId }
    }

    // Изменить запись в карте
    fun updateComponent(cardId: String, componentId: String, newName: String, newLink: String, newPrice: String, newType: ComponentType): Boolean {
        val cardComponents = cards[cardId] ?: throw IllegalArgumentException("Card with ID $cardId does not exist")
        val component = cardComponents.find { it.id == componentId } ?: return false
        component.name = newName
        component.link = newLink
        component.price = newPrice
        component.type = newType
        return true
    }

    // Экспорт данных в базу данных
    fun exportToDatabase(): Map<String, List<Component>> {
        return cards
    }

    // Импорт данных из базы данных
    fun importFromDatabase(data: Map<String, List<Component>>, baseCardIdList: List<String>) {
        // Для каждой карты из данных
        for ((cardId, components) in data) {
            components.forEach { component ->
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

                // Если тип OTHER, создаем новую карту и добавляем компонент в нее
                if (targetCardIndex == null) {
                    // Создаем новую карту для типа OTHER
                    val newCardId = addCard(customCardId = cardId)
                    addComponentToCard(
                        newCardId,
                        name = component.name,
                        link = component.link,
                        price = component.price,
                        type = component.type
                    )
                } else {
                    // Если тип не OTHER, добавляем компонент в существующую карту
                    val targetCardId = baseCardIdList.getOrNull(targetCardIndex)
                    if (targetCardId != null) {
                        addComponentToCard(
                            targetCardId,
                            name = component.name,
                            link = component.link,
                            price = component.price,
                            type = component.type
                        )
                    }
                }
            }
            logCardLibrary()
        }
    }

    fun logCardLibrary() {
        // Проходим по всем картам
        for ((cardId, components) in cards) {
            Log.d("CardLibrary", "card-$cardId")

            // Проходим по всем компонентам внутри карты
            components.forEach { component ->
                Log.d("CardLibrary", "--note")
                Log.d("CardLibrary", "--name: ${component.name}")
                Log.d("CardLibrary", "--url: ${component.link}")
                Log.d("CardLibrary", "--cost: ${component.price}")
                Log.d("CardLibrary", "--id: ${component.id}")
            }
        }
    }

    fun getComponentTypesByCardId(cardId: String): ComponentType? {
        val cardComponents = cards[cardId] ?: throw IllegalArgumentException("Card with ID $cardId does not exist")

        // Возвращаем тип первого компонента, если он существует, иначе возвращаем null
        return cardComponents.firstOrNull()?.type
    }

    // Функция для получения ссылки и цены по ID карты и названию компонента
    fun getComponentLinkAndPriceByCardIdAndName(cardId: String, componentName: String): Pair<String?, String?> {
        val cardComponents = cards[cardId] ?: throw IllegalArgumentException("Card with ID $cardId does not exist")

        // Ищем компонент по названию
        val component = cardComponents.find { it.name == componentName }

        // Если компонент найден, возвращаем пару: ссылка и цена, иначе возвращаем null
        return if (component != null) {
            Pair(component.link, component.price)
        } else {
            Pair(null, null) // Если компонент не найден
        }
    }

}
