package com.onislanguage.app.data.model

import com.onislanguage.app.data.api.TranscriptSegmentDto

data class MediaTranscriptHistoryItem(
    val id: Long,
    val title: String,
    val sourceType: String,
    val sourceUri: String?,
    val localMediaPath: String?,
    val mediaUrl: String?,
    val mediaKind: String?,
    val duration: Float,
    val fullTextJa: String,
    val fullTextVi: String?,
    val segments: List<TranscriptSegmentDto>,
    val createdAt: Long
)
