package com.onislanguage.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onislanguage.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val authState = repository.authState

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.login(email, password)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Đăng nhập thất bại: ${it.message}"
                }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, displayName: String, onOtpSent: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.initiateRegister(email, password, confirmPassword, displayName)
                .onSuccess {
                    _isLoading.value = false
                    onOtpSent()
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Yêu cầu OTP thất bại: ${it.message}"
                }
        }
    }

    fun verifyOtp(email: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.verifyRegister(email, otp)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Xác thực OTP thất bại: ${it.message}"
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun updateProfile(displayName: String?, email: String?, onOtpRequired: () -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.updateProfile(displayName, email)
                .onSuccess { otpRequired ->
                    _isLoading.value = false
                    if (otpRequired && email != null) {
                        onOtpRequired()
                    } else {
                        onSuccess()
                    }
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Cập nhật thất bại: ${it.message}"
                }
        }
    }

    fun verifyEmailChange(newEmail: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.verifyEmailChange(newEmail, otp)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Xác thực email thất bại: ${it.message}"
                }
        }
    }

    fun initiatePasswordChange(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        onOtpSent: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.initiatePasswordChange(currentPassword, newPassword, confirmNewPassword)
                .onSuccess {
                    _isLoading.value = false
                    onOtpSent()
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Yêu cầu OTP đổi mật khẩu thất bại: ${it.message}"
                }
        }
    }

    fun verifyPasswordChange(otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.verifyPasswordChange(otp)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _error.value = "Xác thực OTP đổi mật khẩu thất bại: ${it.message}"
                }
        }
    }
}
