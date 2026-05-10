package com.onislanguage.app.data.repository

import com.onislanguage.app.data.api.KanjiResponseDto
import com.onislanguage.app.data.api.OnisApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AiRepository(private val apiService: OnisApiService) {
    
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

    suspend fun ocrImage(imageFile: File): Result<com.onislanguage.app.data.api.OcrResponseDto> {
        return try {
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val response = apiService.ocrImage(body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun transcribeMedia(mediaFile: File): Result<com.onislanguage.app.data.api.TranscriptionResponseDto> {
        return try {
            val requestFile = mediaFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", mediaFile.name, requestFile)
            val response = apiService.transcribeMedia(body)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
