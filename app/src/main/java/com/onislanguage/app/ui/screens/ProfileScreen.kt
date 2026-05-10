package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
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
    
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(authState?.user?.display_name ?: "") }
    var editEmail by remember { mutableStateOf(authState?.user?.email ?: "") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 128.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Box(modifier = Modifier.size(128.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(20.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)), CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(authState?.user?.display_name?.take(2)?.uppercase() ?: "??", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.1f))
                    .background(Color.White, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                    .clickable { isEditing = !isEditing }
                    .padding(8.dp)
            ) {
                Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (isEditing) {
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text(stringResource(R.string.display_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.updateProfile(
                        displayName = editName,
                        email = if (editEmail != authState?.user?.email) editEmail else null,
                        onOtpRequired = { showOtpDialog = true },
                        onSuccess = { isEditing = false }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else Text(stringResource(R.string.save))
            }
        } else {
            Text(authState?.user?.display_name ?: "Onis Learner", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(authState?.user?.email ?: "guest@onis.app", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App Settings
        SectionHeader("APP SETTINGS")
        SettingsCard {
            SettingsRow(icon = Icons.Default.Translate, label = stringResource(R.string.language), type = "link", iconColor = MaterialTheme.colorScheme.primary, onClick = { onNavigate(com.onislanguage.app.navigation.Screen.Settings.route) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            SettingsRow(
                icon = Icons.Default.Logout, 
                label = stringResource(R.string.logout), 
                type = "button", 
                iconColor = MaterialTheme.colorScheme.error, 
                textColor = MaterialTheme.colorScheme.error,
                onClick = { viewModel.logout() }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("ONISLANGUAGE V1.2.0", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.outline)
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Xác thực thay đổi Email") },
            text = {
                Column {
                    Text("Vui lòng nhập mã OTP đã gửi đến $editEmail")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = otp, onValueChange = { otp = it }, label = { Text(stringResource(R.string.otp_code)) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.verifyEmailChange(editEmail, otp) {
                        showOtpDialog = false
                        isEditing = false
                    }
                }) { Text(stringResource(R.string.verify)) }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 4.dp)) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector, label: String, type: String,
    checked: Boolean? = null, onChanged: ((Boolean) -> Unit)? = null,
    iconBg: Color? = null, iconColor: Color, textColor: Color? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor ?: MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        
        if (type == "link") {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}
