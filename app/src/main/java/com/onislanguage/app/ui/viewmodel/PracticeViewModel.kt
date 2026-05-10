package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onislanguage.app.data.model.PracticeExam
import com.onislanguage.app.data.model.PracticeSubmissionResponse
import com.onislanguage.app.data.repository.PracticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(private val repository: PracticeRepository) : ViewModel() {
    private val _exams = MutableStateFlow<List<PracticeExam>>(emptyList())
    val exams: StateFlow<List<PracticeExam>> = _exams

    private val _currentExam = MutableStateFlow<PracticeExam?>(null)
    val currentExam: StateFlow<PracticeExam?> = _currentExam

    private val _submissionResult = MutableStateFlow<PracticeSubmissionResponse?>(null)
    val submissionResult: StateFlow<PracticeSubmissionResponse?> = _submissionResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadExams() {
        viewModelScope.launch {
            _isLoading.value = true
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

    fun startExam(examId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _submissionResult.value = null
            repository.getFullExam(examId)
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

    fun submitExam(examId: String, answers: Map<String, String>) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.submitExam(examId, answers)
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
}
