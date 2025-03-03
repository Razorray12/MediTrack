package com.example.myapp.placeholder

data class ChatMessage(
    val id: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isSentByMe: Boolean
)

object ChatPlaceholderContent {
    val ITEMS: MutableList<ChatMessage> = mutableListOf()

    init {
        addItem(ChatMessage("1", "Иван", "Привет, как дела?", "10:00", false))
        addItem(ChatMessage("2", "Я", "Все отлично, спасибо!", "10:02", true))
        addItem(ChatMessage("3", "Иван", "Пойдём сегодня на обед?", "10:05", false))
        addItem(ChatMessage("4", "Я", "Хорошая идея, где встречаемся?", "10:07", true))
        addItem(ChatMessage("5", "Иван", "Давайте в кафе на углу.", "10:08", false))
    }

    private fun addItem(item: ChatMessage) {
        ITEMS.add(item)
    }
}
