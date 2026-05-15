package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.geometry.Offset
import com.onislanguage.app.data.api.AnalyzeTextResponseDto
import com.onislanguage.app.data.api.ChatMessageDto
import com.onislanguage.app.data.model.ChatSession
import com.onislanguage.app.data.api.KanjiResponseDto
import com.onislanguage.app.data.model.ChatUiMessage
import com.onislanguage.app.data.model.OcrImageHistoryItem
import com.onislanguage.app.data.model.MediaTranscriptHistoryItem
import com.onislanguage.app.data.model.StudyUsageOverview
import com.onislanguage.app.data.model.TranslationHistoryItem
import com.onislanguage.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AiViewModel(private val repository: AiRepository) : ViewModel() {
    private val japanesePattern = Regex("[\\u3040-\\u30ff\\u31f0-\\u31ff\\u3400-\\u4dbf\\u4e00-\\u9fff\\uff66-\\uff9f々ー]")

    private val _homeTranslationResult = MutableStateFlow<com.onislanguage.app.data.api.TranslationResponse?>(null)
    val homeTranslationResult: StateFlow<com.onislanguage.app.data.api.TranslationResponse?> = _homeTranslationResult

    private val _textAnalysisResult = MutableStateFlow<AnalyzeTextResponseDto?>(null)
    val textAnalysisResult: StateFlow<AnalyzeTextResponseDto?> = _textAnalysisResult

    private val _isLanguageLoading = MutableStateFlow(false)
    val isLanguageLoading: StateFlow<Boolean> = _isLanguageLoading

    private val _languageError = MutableStateFlow<String?>(null)
    val languageError: StateFlow<String?> = _languageError

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions

    private val _currentChatSessionId = MutableStateFlow<String?>(null)
    val currentChatSessionId: StateFlow<String?> = _currentChatSessionId

    private val _chatMessages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatUiMessage>> = _chatMessages

    private val _chatDraft = MutableStateFlow("")
    val chatDraft: StateFlow<String> = _chatDraft

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError
    private val _topChatPrompts = MutableStateFlow<List<String>>(emptyList())
    val topChatPrompts: StateFlow<List<String>> = _topChatPrompts

    private val _activeTranscriptSegmentIndex = MutableStateFlow(-1)
    val activeTranscriptSegmentIndex: StateFlow<Int> = _activeTranscriptSegmentIndex

    private val _kanjiStrokes = MutableStateFlow<List<List<Offset>>>(emptyList())
    val kanjiStrokes: StateFlow<List<List<Offset>>> = _kanjiStrokes

    private val _selectedKanji = MutableStateFlow<String?>(null)
    val selectedKanji: StateFlow<String?> = _selectedKanji

    private val _kanjiResult = MutableStateFlow<KanjiResponseDto?>(null)
    val kanjiResult: StateFlow<KanjiResponseDto?> = _kanjiResult

    private val _ocrResult = MutableStateFlow<com.onislanguage.app.data.api.OcrResponseDto?>(null)
    val ocrResult: StateFlow<com.onislanguage.app.data.api.OcrResponseDto?> = _ocrResult

    private val _translationHistory = MutableStateFlow<List<TranslationHistoryItem>>(emptyList())
    val translationHistory: StateFlow<List<TranslationHistoryItem>> = _translationHistory

    private val _ocrHistory = MutableStateFlow<List<OcrImageHistoryItem>>(emptyList())
    val ocrHistory: StateFlow<List<OcrImageHistoryItem>> = _ocrHistory

    private val _selectedOcrHistory = MutableStateFlow<OcrImageHistoryItem?>(null)
    val selectedOcrHistory: StateFlow<OcrImageHistoryItem?> = _selectedOcrHistory

    private val _latestOcrHistoryId = MutableStateFlow<Long?>(null)
    val latestOcrHistoryId: StateFlow<Long?> = _latestOcrHistoryId

    private val _transcriptionResult = MutableStateFlow<com.onislanguage.app.data.api.TranscriptionResponseDto?>(null)
    val transcriptionResult: StateFlow<com.onislanguage.app.data.api.TranscriptionResponseDto?> = _transcriptionResult

    private val _transcriptionHistory = MutableStateFlow<List<MediaTranscriptHistoryItem>>(emptyList())
    val transcriptionHistory: StateFlow<List<MediaTranscriptHistoryItem>> = _transcriptionHistory

    private val _serverTranscriptionHistory = MutableStateFlow<List<MediaTranscriptHistoryItem>>(emptyList())
    val serverTranscriptionHistory: StateFlow<List<MediaTranscriptHistoryItem>> = _serverTranscriptionHistory

    private val _studyUsageOverview = MutableStateFlow(StudyUsageOverview())
    val studyUsageOverview: StateFlow<StudyUsageOverview> = _studyUsageOverview

    private val _selectedTranscriptHistory = MutableStateFlow<MediaTranscriptHistoryItem?>(null)
    val selectedTranscriptHistory: StateFlow<MediaTranscriptHistoryItem?> = _selectedTranscriptHistory

    private val _latestTranscriptHistoryId = MutableStateFlow<Long?>(null)
    val latestTranscriptHistoryId: StateFlow<Long?> = _latestTranscriptHistoryId

    private val _activeTranscriptLocalPath = MutableStateFlow<String?>(null)
    val activeTranscriptLocalPath: StateFlow<String?> = _activeTranscriptLocalPath

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uploadProgress = MutableStateFlow<Int?>(null)
    val uploadProgress: StateFlow<Int?> = _uploadProgress

    private val _uploadLabel = MutableStateFlow<String?>(null)
    val uploadLabel: StateFlow<String?> = _uploadLabel

    private var processingProgressJob: Job? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        resetChat()
        loadTranscriptionHistory()
        loadOcrHistory()
        loadTranslationHistory()
        refreshStudyUsageOverview()
        refreshTopChatPrompts()
    }

    private fun recordStudyUsage(featureKey: String, featureLabel: String) {
        repository.recordStudyUsage(featureKey, featureLabel)
        _studyUsageOverview.value = repository.getStudyUsageOverview()
    }

    fun refreshStudyUsageOverview() {
        viewModelScope.launch {
            _studyUsageOverview.value = repository.getStudyUsageOverview()
        }
    }

    fun updateChatDraft(value: String) {
        _chatDraft.value = value
    }

    fun clearChatError() {
        _chatError.value = null
    }

    fun refreshTopChatPrompts() {
        viewModelScope.launch {
            _topChatPrompts.value = repository.getTopChatPrompts()
        }
    }

    fun saveTranslationHistory(
        sourceText: String,
        translatedText: String,
        direction: String
    ) {
        if (sourceText.isBlank() || translatedText.isBlank()) return
        viewModelScope.launch {
            repository.saveTranslationHistory(sourceText.trim(), translatedText.trim(), direction)
            loadTranslationHistory()
        }
    }

    fun loadTranslationHistory() {
        viewModelScope.launch {
            _translationHistory.value = repository.getTranslationHistories()
        }
    }

    fun clearLanguageError() {
        _languageError.value = null
    }

    fun clearTextAnalysis() {
        _textAnalysisResult.value = null
        _languageError.value = null
    }

    fun translateForHome(text: String, sourceLanguage: String) {
        val normalized = text.trim()
        if (normalized.isBlank()) {
            _homeTranslationResult.value = null
            _languageError.value = null
            return
        }
        viewModelScope.launch {
            _isLanguageLoading.value = true
            _languageError.value = null
            repository.translateText(
                text = normalized,
                sourceLanguage = sourceLanguage,
                targetLanguage = if (sourceLanguage == "ja") "vi" else "ja"
            ).onSuccess {
                _homeTranslationResult.value = it
                val historyText = if (sourceLanguage == "ja") {
                    it.text_display?.translation_vi?.trim().orEmpty().ifBlank { it.translated_text.trim() }
                } else {
                    it.translated_text.trim()
                }
                if (historyText.isNotBlank()) {
                    saveTranslationHistory(
                        sourceText = normalized,
                        translatedText = historyText,
                        direction = if (sourceLanguage == "ja") "ja_vi" else "vi_ja"
                    )
                }
                recordStudyUsage(AiRepository.FEATURE_TRANSLATE, "Dịch văn bản")
                _isLanguageLoading.value = false
            }.onFailure {
                _languageError.value = "Không dịch được văn bản: ${it.message}"
                _isLanguageLoading.value = false
            }
        }
    }

    fun detectHomeSourceLanguage(text: String): String {
        return if (japanesePattern.containsMatchIn(text)) "ja" else "vi"
    }

    fun analyzeText(text: String, language: String) {
        val normalized = text.trim()
        if (normalized.isBlank()) {
            _textAnalysisResult.value = null
            _languageError.value = null
            return
        }
        viewModelScope.launch {
            _isLanguageLoading.value = true
            _languageError.value = null
            repository.analyzeText(normalized, language)
                .onSuccess {
                    _textAnalysisResult.value = it
                    recordStudyUsage(AiRepository.FEATURE_ANALYZE, "Phân tích văn bản")
                    _isLanguageLoading.value = false
                }
                .onFailure {
                    _languageError.value = "Không phân tích được văn bản: ${it.message}"
                    _isLanguageLoading.value = false
                }
        }
    }

    fun resetChat() {
        val initialSession = buildNewChatSession(index = 1)
        _chatSessions.value = listOf(initialSession)
        _currentChatSessionId.value = initialSession.id
        _chatMessages.value = initialSession.messages
        _chatDraft.value = ""
        _chatError.value = null
    }

    fun createNewChatSession() {
        val nextIndex = _chatSessions.value.size + 1
        val session = buildNewChatSession(index = nextIndex)
        _chatSessions.value = listOf(session) + _chatSessions.value
        _currentChatSessionId.value = session.id
        _chatMessages.value = session.messages
        _chatDraft.value = ""
        _chatError.value = null
    }

    fun selectChatSession(sessionId: String) {
        val session = _chatSessions.value.firstOrNull { it.id == sessionId } ?: return
        _currentChatSessionId.value = session.id
        _chatMessages.value = session.messages
        _chatDraft.value = ""
        _chatError.value = null
    }

    fun sendChatMessage(prefilled: String? = null) {
        val content = (prefilled ?: _chatDraft.value).trim()
        if (content.isBlank() || _isChatSending.value) return

        val userMessage = ChatUiMessage(
            id = "user_${System.currentTimeMillis()}",
            role = "user",
            content = content
        )

        _chatMessages.value = _chatMessages.value + userMessage
        try {
            repository.recordChatPromptUsage(content)
            refreshTopChatPrompts()
        } catch (_: Exception) {
            // Never crash chat UI because of local suggestion stats.
        }
        updateCurrentSessionMessages(_chatMessages.value, preferredTitle = content)
        _chatDraft.value = ""
        _chatError.value = null

        viewModelScope.launch {
            _isChatSending.value = true
            repository.sendChat(
                _chatMessages.value.map { ChatMessageDto(role = it.role, content = it.content) }
            ).onSuccess { response ->
                _chatMessages.value = _chatMessages.value + ChatUiMessage(
                    id = "assistant_${System.currentTimeMillis()}",
                    role = "assistant",
                    content = response.response.trim()
                )
                updateCurrentSessionMessages(_chatMessages.value)
                recordStudyUsage(AiRepository.FEATURE_CHAT, "Chat AI")
                _isChatSending.value = false
            }.onFailure {
                _chatError.value = "Không gửi được tin nhắn. ${it.message ?: "Vui lòng thử lại."}"
                _isChatSending.value = false
            }
        }
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
        viewModelScope.launch {
            repository.clearLocalData(
                clearTranslations = clearTranslations,
                clearOcr = clearOcr,
                clearMedia = clearMedia,
                clearFlashcards = clearFlashcards,
                clearExams = clearExams,
                clearStudyUsage = clearStudyUsage,
                clearChatStats = clearChatStats
            )
            loadTranslationHistory()
            loadOcrHistory()
            loadTranscriptionHistory()
            refreshStudyUsageOverview()
            refreshTopChatPrompts()
        }
    }

    private fun buildNewChatSession(index: Int): ChatSession {
        return ChatSession(
            id = "session_${System.currentTimeMillis()}_$index",
            title = "Phiên $index",
            messages = listOf(
                ChatUiMessage(
                    id = "assistant_welcome_$index",
                    role = "assistant",
                    content = "Xin chào, mình là Akira. Hỏi mình về ngữ pháp, từ vựng, hội thoại hoặc nhờ sửa câu tiếng Nhật."
                )
            )
        )
    }

    private fun updateCurrentSessionMessages(
        messages: List<ChatUiMessage>,
        preferredTitle: String? = null
    ) {
        val currentId = _currentChatSessionId.value ?: return
        _chatSessions.value = _chatSessions.value.map { session ->
            if (session.id != currentId) {
                session
            } else {
                session.copy(
                    title = preferredTitle?.takeIf { session.title.startsWith("Phiên ") }?.take(28) ?: session.title,
                    messages = messages
                )
            }
        }
    }

    fun startKanjiStroke(offset: Offset) {
        if (_kanjiResult.value != null) {
            _kanjiResult.value = null
            _selectedKanji.value = null
        }
        _error.value = null
        _kanjiStrokes.value = _kanjiStrokes.value + listOf(listOf(offset))
    }

    fun appendKanjiStrokePoint(offset: Offset) {
        val strokes = _kanjiStrokes.value
        if (strokes.isEmpty()) return

        val updatedLastStroke = strokes.last() + offset
        _kanjiStrokes.value = strokes.dropLast(1) + listOf(updatedLastStroke)
    }

    fun clearKanjiDrawing() {
        _kanjiStrokes.value = emptyList()
        _kanjiResult.value = null
        _selectedKanji.value = null
        _error.value = null
    }

    fun selectKanji(kanji: String?) {
        _selectedKanji.value = kanji
    }

    fun recognizeKanji(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.recognizeKanji(imageFile)
                .onSuccess {
                    _kanjiResult.value = it
                    _selectedKanji.value = it.top1.kanji
                    recordStudyUsage(AiRepository.FEATURE_KANJI, "Vẽ kanji")
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Nhận diện Kanji thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun ocrImage(imageFile: File, sourceUri: String?, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            startUploadProgress("Đang tải ảnh lên")
            _error.value = null
            repository.ocrImage(imageFile) { sentBytes, totalBytes ->
                updateUploadPhaseProgress(sentBytes, totalBytes)
            }
                .onSuccess {
                    finishProgress()
                    _ocrResult.value = it
                    val historyId = repository.saveOcrHistory(
                        title = title.ifBlank { imageFile.nameWithoutExtension.ifBlank { "OCR image" } },
                        sourceUri = sourceUri,
                        response = it
                    )
                    _latestOcrHistoryId.value = historyId
                    _selectedOcrHistory.value = repository.getOcrHistory(historyId)
                    loadOcrHistory()
                    recordStudyUsage(AiRepository.FEATURE_OCR, "Nhận diện ảnh")
                    _isLoading.value = false
                }
                .onFailure {
                    clearProgress()
                    _error.value = "OCR thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun transcribeMedia(
        mediaFile: File,
        sourceType: String,
        sourceUri: String?,
        displayTitle: String,
        uploadFileName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            startUploadProgress("Đang tải file lên")
            _error.value = null
            repository.transcribeMedia(mediaFile, uploadFileName) { sentBytes, totalBytes ->
                updateUploadPhaseProgress(sentBytes, totalBytes)
            }
                .onSuccess {
                    finishProgress()
                    _transcriptionResult.value = it
                    _activeTranscriptSegmentIndex.value = -1
                    _activeTranscriptLocalPath.value = mediaFile.absolutePath
                    val historyId = repository.saveTranscriptionHistory(
                        title = displayTitle.ifBlank { it.media_title?.takeIf { title -> title.isNotBlank() } ?: "Untitled media" },
                        sourceType = sourceType,
                        sourceUri = sourceUri,
                        response = it,
                        localMediaPath = mediaFile.absolutePath
                    )
                    _latestTranscriptHistoryId.value = historyId
                    _selectedTranscriptHistory.value = repository.getTranscriptionHistory(historyId)
                    _isLoading.value = false
                    loadLocalTranscriptionHistory()
                    refreshServerTranscriptHistory()
                    val feature = if (sourceType == "audio") {
                        AiRepository.FEATURE_AUDIO to "Nhận diện audio"
                    } else {
                        AiRepository.FEATURE_VIDEO to "Nhận diện video"
                    }
                    recordStudyUsage(feature.first, feature.second)
                }
                .onFailure {
                    clearProgress()
                    _error.value = "Chuyển đổi âm thanh thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun transcribeYouTube(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            startProcessingPhase("Đang xử lý video từ YouTube")
            _error.value = null
            repository.transcribeYouTube(url)
                .onSuccess {
                    finishProgress()
                    _transcriptionResult.value = it
                    _activeTranscriptSegmentIndex.value = -1
                    _activeTranscriptLocalPath.value = null
                    val historyId = repository.saveTranscriptionHistory(
                        title = it.media_title?.takeIf { title -> title.isNotBlank() } ?: url,
                        sourceType = "youtube",
                        sourceUri = url,
                        response = it,
                        localMediaPath = null
                    )
                    _latestTranscriptHistoryId.value = historyId
                    _selectedTranscriptHistory.value = repository.getTranscriptionHistory(historyId)
                    _isLoading.value = false
                    loadLocalTranscriptionHistory()
                    refreshServerTranscriptHistory()
                    recordStudyUsage(AiRepository.FEATURE_YOUTUBE, "Nhận diện YouTube")
                }
                .onFailure {
                    clearProgress()
                    _error.value = "Chuyển đổi YouTube thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    private fun startUploadProgress(label: String) {
        processingProgressJob?.cancel()
        _uploadLabel.value = label
        _uploadProgress.value = 0
    }

    private fun updateUploadPhaseProgress(sentBytes: Long, totalBytes: Long) {
        if (totalBytes <= 0L) return
        val uploadPercent = ((sentBytes.coerceAtMost(totalBytes) * 100) / totalBytes).toInt()
        _uploadProgress.value = ((uploadPercent.coerceIn(0, 100) * 70) / 100).coerceIn(0, 70)
        if (uploadPercent >= 100) {
            startProcessingPhase("Đang phân tích dữ liệu")
        }
    }

    private fun startProcessingPhase(label: String) {
        _uploadLabel.value = label
        val startValue = (_uploadProgress.value ?: 70).coerceAtLeast(70)
        _uploadProgress.value = startValue
        processingProgressJob?.cancel()
        processingProgressJob = viewModelScope.launch {
            var current = startValue
            while (current < 95) {
                delay(500)
                current += if (current < 85) 3 else 1
                _uploadProgress.value = current.coerceAtMost(95)
            }
        }
    }

    private fun finishProgress() {
        processingProgressJob?.cancel()
        _uploadLabel.value = "Hoàn tất"
        _uploadProgress.value = 100
        viewModelScope.launch {
            delay(250)
            clearProgress()
        }
    }

    private fun clearProgress() {
        processingProgressJob?.cancel()
        processingProgressJob = null
        _uploadLabel.value = null
        _uploadProgress.value = null
    }

    fun updateActiveTranscriptSegment(positionMs: Int) {
        val segments = _selectedTranscriptHistory.value?.segments ?: _transcriptionResult.value?.segments ?: return
        val positionSeconds = positionMs / 1000f
        val activeIndex = segments.indexOfFirst { positionSeconds >= it.start && positionSeconds <= it.end }
        _activeTranscriptSegmentIndex.value = activeIndex
    }

    fun loadTranscriptionHistory() {
        viewModelScope.launch {
            loadLocalTranscriptionHistory()
            refreshServerTranscriptHistory()
        }
    }

    fun loadOcrHistory() {
        viewModelScope.launch {
            _ocrHistory.value = repository.getOcrHistories()
        }
    }

    fun openOcrHistory(historyId: Long) {
        viewModelScope.launch {
            _selectedOcrHistory.value = repository.getOcrHistory(historyId)
        }
    }

    fun consumeLatestOcrHistoryId() {
        _latestOcrHistoryId.value = null
    }

    fun openTranscriptHistory(historyId: Long) {
        viewModelScope.launch {
            _selectedTranscriptHistory.value = repository.getTranscriptionHistory(historyId)
            _activeTranscriptSegmentIndex.value = -1
        }
    }

    fun openTranscriptHistoryItem(item: MediaTranscriptHistoryItem) {
        _selectedTranscriptHistory.value = item
        _activeTranscriptSegmentIndex.value = -1
    }

    fun importServerTranscriptHistory(item: MediaTranscriptHistoryItem) {
        viewModelScope.launch {
            val historyId = repository.importServerTranscriptionHistory(item)
            _selectedTranscriptHistory.value = repository.getTranscriptionHistory(historyId)
            _latestTranscriptHistoryId.value = historyId
            _activeTranscriptSegmentIndex.value = -1
            loadLocalTranscriptionHistory()
        }
    }

    private suspend fun loadLocalTranscriptionHistory() {
        _transcriptionHistory.value = repository.getTranscriptionHistories()
    }

    private fun refreshServerTranscriptHistory() {
        viewModelScope.launch {
            repository.getServerTranscriptionHistories()
                .onSuccess { _serverTranscriptionHistory.value = it }
                .onFailure { if (_serverTranscriptionHistory.value.isEmpty()) _error.value = null }
        }
    }

    fun consumeLatestTranscriptHistoryId() {
        _latestTranscriptHistoryId.value = null
    }
}
