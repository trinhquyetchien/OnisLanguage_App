package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onislanguage.app.data.model.*
import com.onislanguage.app.data.repository.PracticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(private val repository: PracticeRepository) : ViewModel() {
    private val _exams = MutableStateFlow<List<PracticeExam>>(emptyList())
    val exams: StateFlow<List<PracticeExam>> = _exams

    private val _localExams = MutableStateFlow<List<PracticeExam>>(emptyList())
    val localExams: StateFlow<List<PracticeExam>> = _localExams

    private val _currentExam = MutableStateFlow<PracticeExam?>(null)
    val currentExam: StateFlow<PracticeExam?> = _currentExam

    private val _submissionResult = MutableStateFlow<PracticeSubmissionResponse?>(null)
    val submissionResult: StateFlow<PracticeSubmissionResponse?> = _submissionResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice

    fun clearFeedback() {
        _notice.value = null
        _error.value = null
    }

    fun loadLocalExams() {
        viewModelScope.launch {
            _localExams.value = repository.getLocalExams()
        }
    }

    fun loadExams() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getRemoteExams()
                .onSuccess {
                    _exams.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Không thể tải đề thi: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun startExam(examId: String, isLocal: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _submissionResult.value = null
            val loader = if (isLocal) repository.getLocalExam(examId) else repository.getFullExam(examId)
            loader
                .onSuccess {
                    _currentExam.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Không thể bắt đầu đề thi: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun downloadExam(examId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _notice.value = null
            repository.downloadRemoteExam(examId)
                .onSuccess {
                    loadLocalExams()
                    _notice.value = "Đề thi đã được tải xuống bộ sưu tập cá nhân."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Không thể tải đề thi: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun submitExam(examId: String, answers: Map<String, String>, isLocal: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val submitter = if (isLocal) repository.submitLocalExam(examId, answers) else repository.submitExam(examId, answers)
            submitter
                .onSuccess {
                    _submissionResult.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Nộp bài thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun finishExam() {
        _currentExam.value = null
        _submissionResult.value = null
    }

    fun generateAIExam(topic: String, count: Int, onResult: (List<PracticeQuestion>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _notice.value = null
            repository.generateAIExam(topic, count)
                .onSuccess {
                    onResult(it)
                    _notice.value = "AI đã tạo câu hỏi nháp."
                }
                .onFailure {
                    _error.value = "Không tạo được đề bằng AI: ${it.message}"
                }
            _isLoading.value = false
        }
    }

    fun saveLocalExam(title: String, level: String, questions: List<PracticeQuestion>, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.saveLocalExam(title, level, questions)
                .onSuccess {
                    loadLocalExams()
                    _notice.value = "Đã lưu đề thi vào bộ sưu tập cá nhân."
                    _isLoading.value = false
                    onSaved?.invoke()
                }
                .onFailure {
                    _error.value = "Lưu đề thi thất bại: ${it.message}"
                    _isLoading.value = false
                }
        }
    }

    fun generateExamFromText(title: String, text: String, level: String = "N3", count: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _notice.value = null
            repository.generateAIExamFromText(title, text, level, count)
                .onSuccess {
                    loadLocalExams()
                    _notice.value = "AI đã tạo đề thi vào bộ sưu tập cá nhân."
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = "Không tạo được đề từ file: ${it.message}"
                    _isLoading.value = false
                }
        }
    }
}
