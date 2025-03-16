package com.example.myapp.placeholder

data class ChatMessage(
    val id: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isSentByMe: Boolean
)

