package com.example.meditrack.holders

data class ChatMessage(
    val id: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isSentByMe: Boolean
)

