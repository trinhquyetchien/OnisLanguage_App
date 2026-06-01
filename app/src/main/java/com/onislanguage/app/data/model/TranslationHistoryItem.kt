package com.onislanguage.app.data.model

data class TranslationHistoryItem(
    val id: Long,
    val sourceText: String,
    val translatedText: String,
    val direction: String,
    val createdAt: Long
)
