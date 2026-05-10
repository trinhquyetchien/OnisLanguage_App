package com.onislanguage.app.data.repository

import com.onislanguage.app.data.api.OnisApiService
import com.onislanguage.app.data.model.PracticeExam
import com.onislanguage.app.data.model.PracticeSubmissionRequest
import com.onislanguage.app.data.model.PracticeSubmissionResponse
import com.onislanguage.app.data.local.OnisLocalDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PracticeRepository(
    private val apiService: OnisApiService,
    private val dbHelper: OnisLocalDbHelper
) {
    suspend fun getRemoteExams(): Result<List<PracticeExam>> {
        return try {
            val response = apiService.getPracticeExams()
            Result.success(response.exams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFullExam(examId: String): Result<PracticeExam> {
        return try {
            val response = apiService.getPracticeExam(examId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitExam(examId: String, answers: Map<String, String>): Result<PracticeSubmissionResponse> {
        return try {
            val request = PracticeSubmissionRequest(answers)
            val response = apiService.submitExam(examId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
