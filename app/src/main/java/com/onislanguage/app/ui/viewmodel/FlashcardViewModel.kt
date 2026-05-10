package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onislanguage.app.data.api.FlashcardDeckDto
import com.onislanguage.app.data.api.FlashcardDto
import com.onislanguage.app.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {
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
            repository.downloadDeck(deckId)
                .onSuccess {
                    loadLocalDecks()
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
            repository.createLocalDeck(title, description)
                .onSuccess {
                    loadLocalDecks()
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
            repository.addLocalCard(deckId, front, back, example)
                .onSuccess {
                    selectDeck(deckId)
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Thêm thẻ thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
}
