package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onislanguage.app.data.api.KanjiResponseDto
import com.onislanguage.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AiViewModel(private val repository: AiRepository) : ViewModel() {
    private val _kanjiResult = MutableStateFlow<KanjiResponseDto?>(null)
    val kanjiResult: StateFlow<KanjiResponseDto?> = _kanjiResult

    private val _ocrResult = MutableStateFlow<com.onislanguage.app.data.api.OcrResponseDto?>(null)
    val ocrResult: StateFlow<com.onislanguage.app.data.api.OcrResponseDto?> = _ocrResult

    private val _transcriptionResult = MutableStateFlow<com.onislanguage.app.data.api.TranscriptionResponseDto?>(null)
    val transcriptionResult: StateFlow<com.onislanguage.app.data.api.TranscriptionResponseDto?> = _transcriptionResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun recognizeKanji(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.recognizeKanji(imageFile)
                .onSuccess {
                    _kanjiResult.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Nhận diện Kanji thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun ocrImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.ocrImage(imageFile)
                .onSuccess {
                    _ocrResult.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "OCR thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun transcribeMedia(mediaFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.transcribeMedia(mediaFile)
                .onSuccess {
                    _transcriptionResult.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Chuyển đổi âm thanh thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
}
