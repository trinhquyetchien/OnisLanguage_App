package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.R
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.ui.viewmodel.AuthViewModel

private enum class OtpPurpose { EMAIL_CHANGE, PASSWORD_CHANGE }

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var isEditing by remember { mutableStateOf(true) }
    var editName by remember { mutableStateOf(authState?.user?.display_name ?: "") }
    var editEmail by remember { mutableStateOf(authState?.user?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpPurpose by remember { mutableStateOf(OtpPurpose.EMAIL_CHANGE) }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(authState?.user?.display_name, authState?.user?.email) {
        if (!isEditing) {
            editName = authState?.user?.display_name.orEmpty()
            editEmail = authState?.user?.email.orEmpty()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 128.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(128.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(38.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(38.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            authState?.user?.display_name?.take(2)?.uppercase() ?: "ON",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape)
                            .clickable { isEditing = !isEditing }
                            .padding(10.dp)
                    ) {
                        Icon(
                            if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.display_name)) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text(stringResource(R.string.email)) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Mật khẩu hiện tại") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text("Xác nhận mật khẩu mới") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val emailChanged = editEmail != authState?.user?.email
                            val hasPasswordInput = currentPassword.isNotBlank() || newPassword.isNotBlank() || confirmNewPassword.isNotBlank()

                            if (hasPasswordInput) {
                                viewModel.initiatePasswordChange(
                                    currentPassword = currentPassword,
                                    newPassword = newPassword,
                                    confirmNewPassword = confirmNewPassword
                                ) {
                                    otpPurpose = OtpPurpose.PASSWORD_CHANGE
                                    showOtpDialog = true
                                }
                                return@Button
                            }

                            viewModel.updateProfile(
                                displayName = editName,
                                email = if (emailChanged) editEmail else null,
                                onOtpRequired = {
                                    otpPurpose = OtpPurpose.EMAIL_CHANGE
                                    showOtpDialog = true
                                },
                                onSuccess = { isEditing = false }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                        else Text(stringResource(R.string.save))
                    }
                } else {
                    Text(authState?.user?.display_name ?: "Onis Learner", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        authState?.user?.email ?: "guest@onis.app",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text(if (otpPurpose == OtpPurpose.EMAIL_CHANGE) "Xác thực thay đổi Email" else "Xác thực đổi mật khẩu") },
            text = {
                Column {
                    Text(
                        if (otpPurpose == OtpPurpose.EMAIL_CHANGE) {
                            "Vui lòng nhập mã OTP đã gửi đến $editEmail"
                        } else {
                            "Vui lòng nhập mã OTP đã gửi về email tài khoản của bạn"
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = otp, onValueChange = { otp = it }, label = { Text(stringResource(R.string.otp_code)) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (otpPurpose == OtpPurpose.EMAIL_CHANGE) {
                        viewModel.verifyEmailChange(editEmail, otp) {
                            showOtpDialog = false
                            isEditing = false
                            otp = ""
                        }
                    } else {
                        viewModel.verifyPasswordChange(otp) {
                            showOtpDialog = false
                            isEditing = false
                            currentPassword = ""
                            newPassword = ""
                            confirmNewPassword = ""
                            otp = ""
                        }
                    }
                }) { Text(stringResource(R.string.verify)) }
            }
        )
    }
}
