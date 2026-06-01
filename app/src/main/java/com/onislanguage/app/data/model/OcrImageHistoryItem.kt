package com.onislanguage.app.data.model

import com.onislanguage.app.data.api.JapaneseTextDisplayDto

data class OcrImageHistoryItem(
    val id: Long,
    val title: String,
    val sourceUri: String?,
    val imageUrl: String?,
    val fullText: String,
    val translatedTextVi: String?,
    val textDisplay: JapaneseTextDisplayDto?,
    val createdAt: Long
)
