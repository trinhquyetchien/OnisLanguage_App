package com.onislanguage.app.data.model

data class ChatSession(
    val id: String,
    val title: String,
    val messages: List<ChatUiMessage>
)
