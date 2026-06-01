package com.onislanguage.app.data.repository

import android.content.ContentValues
import com.onislanguage.app.data.api.ChatMessageDto
import com.onislanguage.app.data.api.ChatRequestDto
import com.onislanguage.app.data.api.ChatResponseDto
import com.onislanguage.app.data.api.AnalyzeTextRequestDto
import com.onislanguage.app.data.api.AnalyzeTextResponseDto
import com.onislanguage.app.data.api.KanjiResponseDto
import com.onislanguage.app.data.api.OnisApiService
import com.onislanguage.app.data.api.OcrResponseDto
import com.onislanguage.app.data.api.ProgressRequestBody
import com.onislanguage.app.data.api.FuriganaTokenDto
import com.onislanguage.app.data.api.JapaneseTextDisplayDto
import com.onislanguage.app.data.api.TranscriptSegmentDto
import com.onislanguage.app.data.api.TranscriptWordDto
import com.onislanguage.app.data.api.TranscriptionResponseDto
import com.onislanguage.app.data.api.YouTubeTranscriptionRequestDto
import com.onislanguage.app.data.local.OnisLocalDbHelper
import com.onislanguage.app.data.model.OcrImageHistoryItem
import com.onislanguage.app.data.model.MediaTranscriptHistoryItem
import com.onislanguage.app.data.model.StudyUsageChartPoint
import com.onislanguage.app.data.model.StudyUsageDaySummary
import com.onislanguage.app.data.model.StudyUsageEvent
import com.onislanguage.app.data.model.StudyUsageFeatureCount
import com.onislanguage.app.data.model.StudyUsageOverview
import com.onislanguage.app.data.model.TranslationHistoryItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AiRepository(
    private val apiService: OnisApiService,
    private val dbHelper: OnisLocalDbHelper
) {
    companion object {
        const val FEATURE_TRANSLATE = "translate_text"
        const val FEATURE_ANALYZE = "analyze_text"
        const val FEATURE_CHAT = "chat"
        const val FEATURE_KANJI = "kanji"
        const val FEATURE_OCR = "ocr"
        const val FEATURE_AUDIO = "audio_transcribe"
        const val FEATURE_RECORD = "record_transcribe"
        const val FEATURE_VIDEO = "video_transcribe"
        const val FEATURE_YOUTUBE = "youtube_transcribe"
    }

    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<com.onislanguage.app.data.api.TranslationResponse> {
        return try {
            Result.success(apiService.translate(com.onislanguage.app.data.api.TranslationRequest(text, sourceLanguage, targetLanguage)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeText(
        text: String,
        language: String
    ): Result<AnalyzeTextResponseDto> {
        return try {
            Result.success(apiService.analyzeText(AnalyzeTextRequestDto(text, language)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChat(messages: List<ChatMessageDto>): Result<ChatResponseDto> {
        return try {
            val response = apiService.chat(ChatRequestDto(messages))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    
    suspend fun recognizeKanji(imageFile: File): Result<KanjiResponseDto> {
        return try {
            val requestFile = imageFile.asRequestBody("image/png".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val response = apiService.recognizeKanji(body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveTranslationHistory(
        sourceText: String,
        translatedText: String,
        direction: String
    ): Long {
        val readableDb = dbHelper.readableDatabase
        val writableDb = dbHelper.writableDatabase

        val latestCursor = readableDb.query(
            OnisLocalDbHelper.TABLE_TRANSLATION_HISTORY,
            null,
            "${OnisLocalDbHelper.COLUMN_TRANSLATION_SOURCE_TEXT} = ? AND ${OnisLocalDbHelper.COLUMN_TRANSLATION_DIRECTION} = ?",
            arrayOf(sourceText, direction),
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_TRANSLATION_CREATED_AT} DESC"
        )
        latestCursor.use {
            if (it.moveToFirst()) {
                val historyId = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TRANSLATION_ID))
                val values = ContentValues().apply {
                    put(OnisLocalDbHelper.COLUMN_TRANSLATION_RESULT_TEXT, translatedText)
                    put(OnisLocalDbHelper.COLUMN_TRANSLATION_CREATED_AT, System.currentTimeMillis())
                }
                writableDb.update(
                    OnisLocalDbHelper.TABLE_TRANSLATION_HISTORY,
                    values,
                    "${OnisLocalDbHelper.COLUMN_TRANSLATION_ID} = ?",
                    arrayOf(historyId.toString())
                )
                return historyId
            }
        }

        val values = ContentValues().apply {
            put(OnisLocalDbHelper.COLUMN_TRANSLATION_SOURCE_TEXT, sourceText)
            put(OnisLocalDbHelper.COLUMN_TRANSLATION_RESULT_TEXT, translatedText)
            put(OnisLocalDbHelper.COLUMN_TRANSLATION_DIRECTION, direction)
            put(OnisLocalDbHelper.COLUMN_TRANSLATION_CREATED_AT, System.currentTimeMillis())
        }
        return writableDb.insert(OnisLocalDbHelper.TABLE_TRANSLATION_HISTORY, null, values)
    }

    fun recordChatPromptUsage(prompt: String) {
        val normalized = normalizePrompt(prompt)
        if (normalized.isBlank()) return
        val db = dbHelper.writableDatabase
        val now = System.currentTimeMillis()
        try {
            val cursor = db.query(
                OnisLocalDbHelper.TABLE_CHAT_PROMPT_STATS,
                arrayOf(OnisLocalDbHelper.COLUMN_CHAT_COUNT),
                "${OnisLocalDbHelper.COLUMN_CHAT_PROMPT} = ?",
                arrayOf(normalized),
                null,
                null,
                null,
                "1"
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val currentCount = it.getInt(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_CHAT_COUNT))
                    val updateValues = ContentValues().apply {
                        put(OnisLocalDbHelper.COLUMN_CHAT_COUNT, currentCount + 1)
                        put(OnisLocalDbHelper.COLUMN_CHAT_LAST_USED_AT, now)
                    }
                    db.update(
                        OnisLocalDbHelper.TABLE_CHAT_PROMPT_STATS,
                        updateValues,
                        "${OnisLocalDbHelper.COLUMN_CHAT_PROMPT} = ?",
                        arrayOf(normalized)
                    )
                } else {
                    val insertValues = ContentValues().apply {
                        put(OnisLocalDbHelper.COLUMN_CHAT_PROMPT, normalized)
                        put(OnisLocalDbHelper.COLUMN_CHAT_COUNT, 1)
                        put(OnisLocalDbHelper.COLUMN_CHAT_LAST_USED_AT, now)
                    }
                    db.insert(OnisLocalDbHelper.TABLE_CHAT_PROMPT_STATS, null, insertValues)
                }
            }
        } catch (_: Exception) {
            // Keep chat flow alive even if local stat write fails.
        }
    }

    fun getTopChatPrompts(limit: Int = 8): List<String> {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_CHAT_PROMPT_STATS,
            arrayOf(OnisLocalDbHelper.COLUMN_CHAT_PROMPT),
            null,
            null,
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_CHAT_COUNT} DESC, ${OnisLocalDbHelper.COLUMN_CHAT_LAST_USED_AT} DESC",
            limit.toString()
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_CHAT_PROMPT)))
                }
            }
        }
    }

    private fun normalizePrompt(prompt: String): String {
        return prompt
            .trim()
            .replace(Regex("\\s+"), " ")
            .take(240)
    }

    fun getTranslationHistories(limit: Int = 20): List<TranslationHistoryItem> {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_TRANSLATION_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_TRANSLATION_CREATED_AT} DESC",
            limit.toString()
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        TranslationHistoryItem(
                            id = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TRANSLATION_ID)),
                            sourceText = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TRANSLATION_SOURCE_TEXT)) ?: "",
                            translatedText = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TRANSLATION_RESULT_TEXT)) ?: "",
                            direction = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TRANSLATION_DIRECTION)) ?: "ja_vi",
                            createdAt = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TRANSLATION_CREATED_AT))
                        )
                    )
                }
            }
        }
    }

    fun recordStudyUsage(
        featureKey: String,
        featureLabel: String,
        createdAt: Long = System.currentTimeMillis()
    ) {
        val values = ContentValues().apply {
            put(OnisLocalDbHelper.COLUMN_USAGE_FEATURE_KEY, featureKey)
            put(OnisLocalDbHelper.COLUMN_USAGE_FEATURE_LABEL, featureLabel)
            put(OnisLocalDbHelper.COLUMN_USAGE_CREATED_AT, createdAt)
        }
        dbHelper.writableDatabase.insert(OnisLocalDbHelper.TABLE_STUDY_USAGE_EVENTS, null, values)
    }

    fun getStudyUsageOverview(dayLimit: Int = 14): StudyUsageOverview {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_STUDY_USAGE_EVENTS,
            arrayOf(
                OnisLocalDbHelper.COLUMN_USAGE_FEATURE_KEY,
                OnisLocalDbHelper.COLUMN_USAGE_FEATURE_LABEL,
                OnisLocalDbHelper.COLUMN_USAGE_CREATED_AT
            ),
            null,
            null,
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_USAGE_CREATED_AT} DESC",
            "1000"
        )

        val events: List<StudyUsageEvent> = cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        StudyUsageEvent(
                            featureKey = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_USAGE_FEATURE_KEY)),
                            featureLabel = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_USAGE_FEATURE_LABEL)),
                            createdAt = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_USAGE_CREATED_AT))
                        )
                    )
                }
            }
        }

        if (events.isEmpty()) return StudyUsageOverview()

        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val dayFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault())
        val titleFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale("vi"))

        val eventsByDate: Map<LocalDate, List<StudyUsageEvent>> =
            events.groupBy { event -> Instant.ofEpochMilli(event.createdAt).atZone(zoneId).toLocalDate() }

        val chartPoints = (6L downTo 0L).map { daysAgo ->
            val date = today.minusDays(daysAgo)
            StudyUsageChartPoint(
                label = dayFormatter.format(date),
                totalCount = eventsByDate[date]?.size ?: 0
            )
        }

        val thisWeekDates = (0L..6L).map { today.minusDays(it) }.toSet()
        val lastWeekDates = (7L..13L).map { today.minusDays(it) }.toSet()
        val thisWeekTotal = eventsByDate.filterKeys { date -> date in thisWeekDates }.values.sumOf { dayEvents -> dayEvents.size }
        val lastWeekTotal = eventsByDate.filterKeys { date -> date in lastWeekDates }.values.sumOf { dayEvents -> dayEvents.size }
        val weeklyDeltaPercent = when {
            lastWeekTotal == 0 && thisWeekTotal == 0 -> 0
            lastWeekTotal == 0 -> 100
            else -> (((thisWeekTotal - lastWeekTotal) * 100f) / lastWeekTotal).toInt()
        }

        val days = eventsByDate.entries
            .sortedByDescending { it.key }
            .take(dayLimit)
            .map { (date, dayEvents) ->
                val featureCounts = dayEvents
                    .groupBy { event -> event.featureKey }
                    .map { (_, featureEvents: List<StudyUsageEvent>) ->
                        StudyUsageFeatureCount(
                            featureKey = featureEvents.first().featureKey,
                            featureLabel = featureEvents.first().featureLabel,
                            count = featureEvents.size
                        )
                    }
                    .sortedByDescending { it.count }

                StudyUsageDaySummary(
                    dateKey = date.toString(),
                    displayDate = titleFormatter.format(date).replaceFirstChar { it.titlecase(Locale("vi")) },
                    totalCount = dayEvents.size,
                    featureCounts = featureCounts
                )
            }

        return StudyUsageOverview(
            thisWeekTotal = thisWeekTotal,
            lastWeekTotal = lastWeekTotal,
            weeklyDeltaPercent = weeklyDeltaPercent,
            chartPoints = chartPoints,
            days = days
        )
    }

    fun clearLocalData(
        clearTranslations: Boolean,
        clearOcr: Boolean,
        clearMedia: Boolean,
        clearFlashcards: Boolean,
        clearExams: Boolean,
        clearStudyUsage: Boolean,
        clearChatStats: Boolean
    ) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            if (clearTranslations) db.delete(OnisLocalDbHelper.TABLE_TRANSLATION_HISTORY, null, null)
            if (clearOcr) db.delete(OnisLocalDbHelper.TABLE_OCR_HISTORY, null, null)
            if (clearMedia) db.delete(OnisLocalDbHelper.TABLE_MEDIA_TRANSCRIPTS, null, null)
            if (clearFlashcards) {
                db.delete(OnisLocalDbHelper.TABLE_CARDS, null, null)
                db.delete(OnisLocalDbHelper.TABLE_DECKS, null, null)
            }
            if (clearExams) {
                db.delete(OnisLocalDbHelper.TABLE_QUESTIONS, null, null)
                db.delete(OnisLocalDbHelper.TABLE_EXAMS, null, null)
            }
            if (clearStudyUsage) db.delete(OnisLocalDbHelper.TABLE_STUDY_USAGE_EVENTS, null, null)
            if (clearChatStats) db.delete(OnisLocalDbHelper.TABLE_CHAT_PROMPT_STATS, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun ocrImage(
        imageFile: File,
        onProgress: (sentBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
    ): Result<OcrResponseDto> {
        return try {
            val requestFile = ProgressRequestBody(
                imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull()),
                onProgress
            )
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val response = apiService.ocrImage(body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveOcrHistory(
        title: String,
        sourceUri: String?,
        response: OcrResponseDto
    ): Long {
        val values = ContentValues().apply {
            put(OnisLocalDbHelper.COLUMN_OCR_TITLE, title)
            put(OnisLocalDbHelper.COLUMN_OCR_SOURCE_URI, sourceUri)
            put(OnisLocalDbHelper.COLUMN_OCR_IMAGE_URL, response.image_url)
            put(OnisLocalDbHelper.COLUMN_OCR_FULL_TEXT, response.full_text)
            put(OnisLocalDbHelper.COLUMN_OCR_TRANSLATED_TEXT, response.translated_text_vi)
            put(OnisLocalDbHelper.COLUMN_OCR_TEXT_DISPLAY_JSON, response.text_display?.let(::textDisplayToJson)?.toString())
            put(OnisLocalDbHelper.COLUMN_OCR_CREATED_AT, System.currentTimeMillis())
        }
        return dbHelper.writableDatabase.insert(OnisLocalDbHelper.TABLE_OCR_HISTORY, null, values)
    }

    fun getOcrHistories(): List<OcrImageHistoryItem> {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_OCR_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_OCR_CREATED_AT} DESC"
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        OcrImageHistoryItem(
                            id = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_ID)),
                            title = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_TITLE)),
                            sourceUri = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_SOURCE_URI)),
                            imageUrl = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_IMAGE_URL)),
                            fullText = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_FULL_TEXT)) ?: "",
                            translatedTextVi = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_TRANSLATED_TEXT)),
                            textDisplay = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_TEXT_DISPLAY_JSON))
                                ?.takeIf { json -> json.isNotBlank() }
                                ?.let { json -> jsonToTextDisplay(JSONObject(json)) },
                            createdAt = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_CREATED_AT))
                        )
                    )
                }
            }
        }
    }

    fun getOcrHistory(historyId: Long): OcrImageHistoryItem? {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_OCR_HISTORY,
            null,
            "${OnisLocalDbHelper.COLUMN_OCR_ID} = ?",
            arrayOf(historyId.toString()),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (!it.moveToFirst()) return null
            OcrImageHistoryItem(
                id = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_ID)),
                title = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_TITLE)),
                sourceUri = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_SOURCE_URI)),
                imageUrl = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_IMAGE_URL)),
                fullText = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_FULL_TEXT)) ?: "",
                translatedTextVi = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_TRANSLATED_TEXT)),
                textDisplay = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_TEXT_DISPLAY_JSON))
                    ?.takeIf { json -> json.isNotBlank() }
                    ?.let { json -> jsonToTextDisplay(JSONObject(json)) },
                createdAt = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OCR_CREATED_AT))
            )
        }
    }

    suspend fun transcribeMedia(
        mediaFile: File,
        uploadFileName: String,
        onProgress: (sentBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
    ): Result<com.onislanguage.app.data.api.TranscriptionResponseDto> {
        return try {
            val requestFile = ProgressRequestBody(
                mediaFile.asRequestBody("*/*".toMediaTypeOrNull()),
                onProgress
            )
            val body = MultipartBody.Part.createFormData("file", uploadFileName, requestFile)
            val response = apiService.transcribeMedia(body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transcribeYouTube(url: String): Result<com.onislanguage.app.data.api.TranscriptionResponseDto> {
        return try {
            val response = apiService.transcribeYouTube(YouTubeTranscriptionRequestDto(url))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServerTranscriptionHistories(): Result<List<MediaTranscriptHistoryItem>> {
        return try {
            val response = apiService.getServerTranscriptionHistory()
            Result.success(
                response.map {
                    MediaTranscriptHistoryItem(
                        id = it.history_id,
                        title = it.title,
                        sourceType = it.source_type,
                        sourceUri = it.source_uri,
                        localMediaPath = null,
                        mediaUrl = it.media_url,
                        mediaKind = it.media_kind,
                        duration = it.duration,
                        fullTextJa = it.full_text_ja,
                        fullTextVi = it.full_text_vi,
                        segments = it.segments,
                        createdAt = 0L
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveTranscriptionHistory(
        title: String,
        sourceType: String,
        sourceUri: String?,
        response: TranscriptionResponseDto,
        localMediaPath: String? = null
    ): Long {
        val values = ContentValues().apply {
            put(OnisLocalDbHelper.COLUMN_MEDIA_TITLE, title)
            put(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_TYPE, sourceType)
            put(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_URI, sourceUri)
            put(OnisLocalDbHelper.COLUMN_MEDIA_LOCAL_PATH, localMediaPath)
            put(OnisLocalDbHelper.COLUMN_MEDIA_REMOTE_URL, response.media_url)
            put(OnisLocalDbHelper.COLUMN_MEDIA_KIND, response.media_kind)
            put(OnisLocalDbHelper.COLUMN_MEDIA_DURATION, response.duration)
            put(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_JA, response.full_text_ja)
            put(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_VI, response.full_text_vi)
            put(OnisLocalDbHelper.COLUMN_MEDIA_SEGMENTS_JSON, serializeSegments(response.segments))
            put(OnisLocalDbHelper.COLUMN_MEDIA_CREATED_AT, System.currentTimeMillis())
        }
        return dbHelper.writableDatabase.insert(OnisLocalDbHelper.TABLE_MEDIA_TRANSCRIPTS, null, values)
    }

    fun importServerTranscriptionHistory(item: MediaTranscriptHistoryItem): Long {
        val existingCursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_MEDIA_TRANSCRIPTS,
            arrayOf(OnisLocalDbHelper.COLUMN_MEDIA_ID),
            "${OnisLocalDbHelper.COLUMN_MEDIA_REMOTE_URL} = ? AND ${OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_JA} = ?",
            arrayOf(item.mediaUrl, item.fullTextJa),
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_MEDIA_CREATED_AT} DESC",
            "1"
        )
        existingCursor.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_ID))
            }
        }

        val values = ContentValues().apply {
            put(OnisLocalDbHelper.COLUMN_MEDIA_TITLE, item.title)
            put(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_TYPE, item.sourceType)
            put(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_URI, item.sourceUri)
            put(OnisLocalDbHelper.COLUMN_MEDIA_LOCAL_PATH, item.localMediaPath)
            put(OnisLocalDbHelper.COLUMN_MEDIA_REMOTE_URL, item.mediaUrl)
            put(OnisLocalDbHelper.COLUMN_MEDIA_KIND, item.mediaKind)
            put(OnisLocalDbHelper.COLUMN_MEDIA_DURATION, item.duration)
            put(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_JA, item.fullTextJa)
            put(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_VI, item.fullTextVi)
            put(OnisLocalDbHelper.COLUMN_MEDIA_SEGMENTS_JSON, serializeSegments(item.segments))
            put(OnisLocalDbHelper.COLUMN_MEDIA_CREATED_AT, System.currentTimeMillis())
        }
        return dbHelper.writableDatabase.insert(OnisLocalDbHelper.TABLE_MEDIA_TRANSCRIPTS, null, values)
    }

    fun getTranscriptionHistories(): List<MediaTranscriptHistoryItem> {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_MEDIA_TRANSCRIPTS,
            null,
            null,
            null,
            null,
            null,
            "${OnisLocalDbHelper.COLUMN_MEDIA_CREATED_AT} DESC"
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        MediaTranscriptHistoryItem(
                            id = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_ID)),
                            title = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_TITLE)),
                            sourceType = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_TYPE)),
                            sourceUri = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_URI)),
                            localMediaPath = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_LOCAL_PATH)),
                            mediaUrl = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_REMOTE_URL)),
                            mediaKind = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_KIND)),
                            duration = it.getFloat(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_DURATION)),
                            fullTextJa = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_JA)) ?: "",
                            fullTextVi = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_VI)),
                            segments = deserializeSegments(
                                it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_SEGMENTS_JSON)) ?: "[]"
                            ),
                            createdAt = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_CREATED_AT))
                        )
                    )
                }
            }
        }
    }

    fun getTranscriptionHistory(historyId: Long): MediaTranscriptHistoryItem? {
        val cursor = dbHelper.readableDatabase.query(
            OnisLocalDbHelper.TABLE_MEDIA_TRANSCRIPTS,
            null,
            "${OnisLocalDbHelper.COLUMN_MEDIA_ID} = ?",
            arrayOf(historyId.toString()),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (!it.moveToFirst()) return null
            MediaTranscriptHistoryItem(
                id = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_ID)),
                title = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_TITLE)),
                sourceType = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_TYPE)),
                sourceUri = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_SOURCE_URI)),
                localMediaPath = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_LOCAL_PATH)),
                mediaUrl = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_REMOTE_URL)),
                mediaKind = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_KIND)),
                duration = it.getFloat(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_DURATION)),
                fullTextJa = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_JA)) ?: "",
                fullTextVi = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_FULL_TEXT_VI)),
                segments = deserializeSegments(
                    it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_SEGMENTS_JSON)) ?: "[]"
                ),
                createdAt = it.getLong(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_MEDIA_CREATED_AT))
            )
        }
    }

    private fun serializeSegments(segments: List<TranscriptSegmentDto>): String {
        return JSONArray().apply {
            segments.forEach { segment ->
                put(
                    JSONObject().apply {
                        put("segment_id", segment.segment_id)
                        put("start", segment.start)
                        put("end", segment.end)
                        put("text_ja", segment.text_ja)
                        put("text_vi", segment.text_vi)
                        segment.text_display?.let { put("text_display", textDisplayToJson(it)) }
                        put("words", JSONArray().apply {
                            segment.words.forEach { word ->
                                put(
                                    JSONObject().apply {
                                        put("start", word.start)
                                        put("end", word.end)
                                        put("text_ja", word.text_ja)
                                        if (word.confidence != null) put("confidence", word.confidence)
                                        word.text_display?.let { put("text_display", textDisplayToJson(it)) }
                                    }
                                )
                            }
                        })
                    }
                )
            }
        }.toString()
    }

    private fun deserializeSegments(json: String): List<TranscriptSegmentDto> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    TranscriptSegmentDto(
                        segment_id = item.optInt("segment_id"),
                        start = item.optDouble("start").toFloat(),
                        end = item.optDouble("end").toFloat(),
                        text_ja = item.optString("text_ja"),
                        text_vi = item.optString("text_vi").takeIf { it.isNotBlank() },
                        text_display = item.optJSONObject("text_display")?.let(::jsonToTextDisplay),
                        words = deserializeWords(item.optJSONArray("words"))
                    )
                )
            }
        }
    }

    private fun deserializeWords(array: JSONArray?): List<TranscriptWordDto> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    TranscriptWordDto(
                        start = item.optDouble("start").toFloat(),
                        end = item.optDouble("end").toFloat(),
                        text_ja = item.optString("text_ja"),
                        confidence = item.optDouble("confidence").takeIf { !it.isNaN() }?.toFloat(),
                        text_display = item.optJSONObject("text_display")?.let(::jsonToTextDisplay)
                    )
                )
            }
        }
    }

    private fun textDisplayToJson(display: JapaneseTextDisplayDto): JSONObject {
        return JSONObject().apply {
            put("text", display.text)
            put("translation_vi", display.translation_vi)
            put("furigana_text", display.furigana_text)
            put("tokens", JSONArray().apply {
                display.tokens.forEach { token ->
                    put(
                        JSONObject().apply {
                            put("surface", token.surface)
                            put("reading", token.reading)
                            put("has_kanji", token.has_kanji)
                            put("part_of_speech", token.part_of_speech)
                        }
                    )
                }
            })
        }
    }

    private fun jsonToTextDisplay(item: JSONObject): JapaneseTextDisplayDto {
        return JapaneseTextDisplayDto(
            text = item.optString("text"),
            translation_vi = item.optString("translation_vi").takeIf { it.isNotBlank() },
            furigana_text = item.optString("furigana_text").takeIf { it.isNotBlank() },
            tokens = buildList {
                val tokensArray = item.optJSONArray("tokens") ?: JSONArray()
                for (index in 0 until tokensArray.length()) {
                    val token = tokensArray.getJSONObject(index)
                    add(
                        FuriganaTokenDto(
                            surface = token.optString("surface"),
                            reading = token.optString("reading").takeIf { it.isNotBlank() },
                            has_kanji = token.optBoolean("has_kanji"),
                            part_of_speech = token.optString("part_of_speech").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        )
    }
}
