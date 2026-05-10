package com.onislanguage.app.data.repository

import android.content.Context
import com.onislanguage.app.data.api.AuthRequest
import com.onislanguage.app.data.api.AuthResponse
import com.onislanguage.app.data.api.OnisApiService
import com.onislanguage.app.data.api.ProfileUpdateRequestDto
import com.onislanguage.app.data.api.EmailChangeVerifyRequestDto
import com.onislanguage.app.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val apiService: OnisApiService,
    private val context: Context
) {
    private val _authState = MutableStateFlow<AuthResponse?>(null)
    val authState: StateFlow<AuthResponse?> = _authState

    suspend fun initialize() {
        // In a real app, you'd fetch user info using the token to populate _authState
        val token = TokenManager.getToken(context).first()
        if (token != null) {
            // Mock auth state for demo
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(AuthRequest(email, password))
            TokenManager.saveToken(context, response.access_token, response.user.user_id)
            _authState.value = response
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiateRegister(email: String, password: String, confirmPassword: String, displayName: String): Result<com.onislanguage.app.data.api.OtpResponse> {
        return try {
            val response = apiService.initiateRegister(AuthRequest(email, password, confirmPassword, displayName))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyRegister(email: String, otp: String): Result<AuthResponse> {
        return try {
            val response = apiService.verifyRegister(com.onislanguage.app.data.api.OtpVerifyRequest(email, otp))
            TokenManager.saveToken(context, response.access_token, response.user.user_id)
            _authState.value = response
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        TokenManager.clearToken(context)
        _authState.value = null
    }

    suspend fun getAccessToken(): String? {
        return TokenManager.getToken(context).first()
    }

    suspend fun updateProfile(displayName: String?, email: String?): Result<Boolean> {
        return try {
            val response = apiService.updateProfile(ProfileUpdateRequestDto(display_name = displayName, email = email))
            if (response.isSuccessful) {
                // If it returns 200, it might be an OtpResponse (if email changed)
                Result.success(true)
            } else {
                Result.failure(Exception("Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyEmailChange(newEmail: String, otp: String): Result<com.onislanguage.app.data.api.AuthUser> {
        return try {
            val response = apiService.verifyEmailChange(EmailChangeVerifyRequestDto(newEmail, otp))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
