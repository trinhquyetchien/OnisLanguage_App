package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onislanguage.app.data.api.FlashcardDeckDto
import com.onislanguage.app.data.api.FlashcardDto
import com.onislanguage.app.data.api.KanjiItemDto
import com.onislanguage.app.data.api.VocabularyItemDto
import com.onislanguage.app.data.repository.AiRepository
import com.onislanguage.app.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val repository: FlashcardRepository,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _localDecks = MutableStateFlow<List<FlashcardDeckDto>>(emptyList())
    val localDecks: StateFlow<List<FlashcardDeckDto>> = _localDecks

    private val _remoteDecks = MutableStateFlow<List<FlashcardDeckDto>>(emptyList())
    val remoteDecks: StateFlow<List<FlashcardDeckDto>> = _remoteDecks

    private val _currentCards = MutableStateFlow<List<FlashcardDto>>(emptyList())
    val currentCards: StateFlow<List<FlashcardDto>> = _currentCards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _importNotice = MutableStateFlow<String?>(null)
    val importNotice: StateFlow<String?> = _importNotice

    fun clearFeedback() {
        _error.value = null
        _importNotice.value = null
    }

    fun loadLocalDecks() {
        viewModelScope.launch {
            _localDecks.value = repository.getLocalDecks()
        }
    }

    fun loadRemoteDecks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRemoteDecks()
                .onSuccess {
                    _remoteDecks.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Không thể tải bộ thẻ từ server: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun downloadDeck(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _importNotice.value = null
            repository.downloadDeck(deckId)
                .onSuccess {
                    loadLocalDecks()
                    _importNotice.value = "Đã tải bộ flashcard về máy."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Tải bộ thẻ thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun selectDeck(deckId: String) {
        viewModelScope.launch {
            _currentCards.value = repository.getLocalCards(deckId)
        }
    }

    fun createDeck(title: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _importNotice.value = null
            repository.createLocalDeck(title, description)
                .onSuccess {
                    loadLocalDecks()
                    _importNotice.value = "Đã tạo bộ flashcard mới."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Tạo bộ thẻ thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun addCard(deckId: String, front: String, back: String, example: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.addLocalCard(deckId, front, back, example)
                .onSuccess {
                    selectDeck(deckId)
                    loadLocalDecks()
                    _importNotice.value = "Đã thêm thẻ mới."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Thêm thẻ thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun addAnalysisItemsToDeck(
        vocabulary: List<VocabularyItemDto>,
        kanji: List<KanjiItemDto>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val vocabularyCards = vocabulary.map {
                FlashcardDto(
                    card_id = "",
                    front = it.surface,
                    back = it.meaning_vi,
                    example_sentence = it.reading
                )
            }
            val kanjiCards = kanji.map {
                FlashcardDto(
                    card_id = "",
                    front = it.kanji,
                    back = it.meaning_vi ?: "",
                    example_sentence = it.reading ?: ""
                )
            }
            val cards = (vocabularyCards + kanjiCards)
                .filter { it.front.isNotBlank() && it.back.isNotBlank() }
                .distinctBy { "${it.front}|${it.back}" }

            if (cards.isEmpty()) {
                _error.value = "Không có từ vựng hoặc kanji hợp lệ để tạo flashcard."
                _isLoading.value = false
                return@launch
            }

            repository.addAnalysisCards("Phân tích văn bản", cards)
                .onSuccess {
                    loadLocalDecks()
                    _importNotice.value = "Đã thêm ${cards.size} thẻ từ phân tích."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Tạo flashcard thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun generateDeckFromText(deckTitle: String, text: String) {
        val normalizedTitle = deckTitle.trim().ifBlank { "Flashcard AI" }
        val normalizedText = text.trim()
        if (normalizedText.isBlank()) {
            _error.value = "File không có nội dung để tạo flashcard."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _importNotice.value = null
            val language = if (normalizedText.any { it.code in 0x3040..0x30ff || it.code in 0x4e00..0x9fff }) "ja" else "vi"
            aiRepository.analyzeText(normalizedText, language)
                .onSuccess { response ->
                    val cards = response.analysis.vocabulary
                        .distinctBy { "${it.surface}|${it.meaning_vi}" }
                        .map {
                            FlashcardDto(
                                card_id = "",
                                front = it.surface,
                                back = it.meaning_vi,
                                example_sentence = it.reading
                            )
                        }

                    if (cards.isEmpty()) {
                        _error.value = "AI không trích xuất được từ vựng để tạo flashcard."
                        _isLoading.value = false
                        return@onSuccess
                    }

                    repository.addAnalysisCards(normalizedTitle, cards)
                        .onSuccess {
                            loadLocalDecks()
                            _importNotice.value = "Đã tạo ${cards.size} flashcard từ file."
                            _isLoading.value = false
                        }
                        .onFailure {
                            _error.value = "Lưu flashcard thất bại: ${it.message}"
                            _isLoading.value = false
                        }
                }
                .onFailure {
                    _error.value = "AI không đọc được file: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun renameDeck(deckId: String, title: String, description: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _importNotice.value = null
            repository.renameLocalDeck(deckId, title, description)
                .onSuccess {
                    loadLocalDecks()
                    _importNotice.value = "Đã đổi tên bộ flashcard."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Đổi tên bộ thẻ thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _importNotice.value = null
            repository.deleteLocalDeck(deckId)
                .onSuccess {
                    if (_currentCards.value.isNotEmpty()) {
                        _currentCards.value = emptyList()
                    }
                    loadLocalDecks()
                    _importNotice.value = "Đã xóa bộ flashcard."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Xóa bộ thẻ thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun clearAllDecks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _importNotice.value = null
            repository.clearAllLocalDecks()
                .onSuccess {
                    _currentCards.value = emptyList()
                    loadLocalDecks()
                    _importNotice.value = "Đã xóa toàn bộ flashcard local."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Không thể dọn bộ flashcard: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
}
