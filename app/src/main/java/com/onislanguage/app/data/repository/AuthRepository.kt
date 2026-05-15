package com.onislanguage.app.data.repository

import android.content.Context
import com.onislanguage.app.data.api.AuthRequest
import com.onislanguage.app.data.api.AuthResponse
import com.onislanguage.app.data.api.AuthUser
import com.onislanguage.app.data.api.OnisApiService
import com.onislanguage.app.data.api.ProfileUpdateRequestDto
import com.onislanguage.app.data.api.EmailChangeVerifyRequestDto
import com.onislanguage.app.data.api.PasswordChangeRequestDto
import com.onislanguage.app.data.api.PasswordChangeVerifyRequestDto
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
        val stored = TokenManager.getStoredUser(context).first()
        if (!stored.token.isNullOrBlank() && !stored.userId.isNullOrBlank() && !stored.email.isNullOrBlank()) {
            _authState.value = AuthResponse(
                access_token = stored.token,
                token_type = "bearer",
                user = AuthUser(
                    user_id = stored.userId,
                    email = stored.email,
                    display_name = stored.displayName ?: stored.email.substringBefore("@")
                )
            )
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(AuthRequest(email, password))
            TokenManager.saveToken(
                context,
                response.access_token,
                response.user.user_id,
                response.user.email,
                response.user.display_name
            )
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
            TokenManager.saveToken(
                context,
                response.access_token,
                response.user.user_id,
                response.user.email,
                response.user.display_name
            )
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
                if (email == null && displayName != null) {
                    val current = _authState.value
                    if (current != null) {
                        val updatedUser = current.user.copy(display_name = displayName)
                        _authState.value = current.copy(user = updatedUser)
                        TokenManager.saveUserProfile(
                            context,
                            updatedUser.user_id,
                            updatedUser.email,
                            updatedUser.display_name
                        )
                    }
                }
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
            val current = _authState.value
            if (current != null) {
                val updatedUser = current.user.copy(email = response.email, display_name = response.display_name)
                _authState.value = current.copy(user = updatedUser)
                TokenManager.saveUserProfile(
                    context,
                    updatedUser.user_id,
                    updatedUser.email,
                    updatedUser.display_name
                )
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiatePasswordChange(currentPassword: String, newPassword: String, confirmNewPassword: String): Result<com.onislanguage.app.data.api.OtpResponse> {
        return try {
            val response = apiService.initiatePasswordChange(
                PasswordChangeRequestDto(
                    current_password = currentPassword,
                    new_password = newPassword,
                    confirm_new_password = confirmNewPassword
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordChange(otp: String): Result<com.onislanguage.app.data.api.AuthUser> {
        return try {
            val response = apiService.verifyPasswordChange(PasswordChangeVerifyRequestDto(otp = otp))
            val current = _authState.value
            if (current != null) {
                val updatedUser = current.user.copy(email = response.email, display_name = response.display_name)
                _authState.value = current.copy(user = updatedUser)
                TokenManager.saveUserProfile(
                    context,
                    updatedUser.user_id,
                    updatedUser.email,
                    updatedUser.display_name
                )
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
