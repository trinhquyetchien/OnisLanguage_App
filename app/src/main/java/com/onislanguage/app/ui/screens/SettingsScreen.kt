package com.onislanguage.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.R
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.ui.viewmodel.AiViewModel
import com.onislanguage.app.ui.viewmodel.AuthViewModel
import com.onislanguage.app.utils.LanguageManager

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    aiViewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    }),
    authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn = authState != null
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLang by remember { mutableStateOf(LanguageManager.normalizeLanguageCode(LanguageManager.getLanguage(context))) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        HeaderSection(stringResource(R.string.settings), "Điều hướng tài khoản, lịch sử và cấu hình ứng dụng từ một nơi gọn hơn.")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isLoggedIn) onNavigate(Screen.Profile.route) else onNavigate(Screen.Login.route)
                    }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (authState?.user?.display_name?.take(2)?.uppercase() ?: "ON"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isLoggedIn) (authState?.user?.display_name ?: "Người dùng Onis") else "Tài khoản",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isLoggedIn) (authState?.user?.email ?: stringResource(R.string.account_syncing)) else stringResource(R.string.login_to_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isLoggedIn) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (isLoggedIn) Icons.Default.ManageAccounts else Icons.Default.Login,
                            contentDescription = null,
                            tint = if (isLoggedIn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLoggedIn) "Quản lý" else "Đăng nhập",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoggedIn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Text("Workspace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SettingsActionItem(
            icon = Icons.Default.History,
            title = "Lịch sử học tập",
            subtitle = "Tài liệu, transcript và hoạt động gần đây",
            onClick = { onNavigate(Screen.History.route) }
        )
        SettingsActionItem(
            icon = Icons.Default.DeleteSweep,
            title = stringResource(R.string.clear_local_data),
            subtitle = stringResource(R.string.history),
            onClick = { showClearDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        SettingsToggleItem(
            icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
            title = stringResource(R.string.dark_mode),
            checked = isDarkTheme,
            onCheckedChange = onThemeChange
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        SettingsActionItem(
            icon = Icons.Default.Translate,
            title = stringResource(R.string.language),
            subtitle = when (currentLang) {
                "ja" -> stringResource(R.string.japanese)
                "en" -> stringResource(R.string.english)
                else -> stringResource(R.string.vietnamese)
            },
            onClick = { showLanguageDialog = true }
        )
        
        if (isLoggedIn) {
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedButton(
                onClick = { 
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = {
                Column {
                    LanguageOption(
                        label = stringResource(R.string.vietnamese),
                        selected = currentLang == "vi",
                        onClick = {
                            currentLang = "vi"
                            LanguageManager.setLanguage(context, "vi")
                            context.findActivity()?.recreate()
                            showLanguageDialog = false
                        }
                    )
                    LanguageOption(
                        label = stringResource(R.string.japanese),
                        selected = currentLang == "ja",
                        onClick = {
                            currentLang = "ja"
                            LanguageManager.setLanguage(context, "ja")
                            context.findActivity()?.recreate()
                            showLanguageDialog = false
                        }
                    )
                    LanguageOption(
                        label = stringResource(R.string.english),
                        selected = currentLang == "en",
                        onClick = {
                            currentLang = "en"
                            LanguageManager.setLanguage(context, "en")
                            context.findActivity()?.recreate()
                            showLanguageDialog = false
                        }
                    )
                }
            }
        )
    }

    if (showClearDialog) {
        var clearTranslations by remember { mutableStateOf(false) }
        var clearOcr by remember { mutableStateOf(false) }
        var clearMedia by remember { mutableStateOf(false) }
        var clearFlashcards by remember { mutableStateOf(false) }
        var clearExams by remember { mutableStateOf(false) }
        var clearUsage by remember { mutableStateOf(false) }
        var clearChat by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_local_data)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ClearCheckbox(stringResource(R.string.clear_translation_history), clearTranslations) { clearTranslations = it }
                    ClearCheckbox(stringResource(R.string.clear_ocr_history), clearOcr) { clearOcr = it }
                    ClearCheckbox(stringResource(R.string.clear_media_history), clearMedia) { clearMedia = it }
                    ClearCheckbox(stringResource(R.string.clear_flashcards_local), clearFlashcards) { clearFlashcards = it }
                    ClearCheckbox(stringResource(R.string.clear_exams_local), clearExams) { clearExams = it }
                    ClearCheckbox(stringResource(R.string.clear_study_usage), clearUsage) { clearUsage = it }
                    ClearCheckbox(stringResource(R.string.clear_chat_suggestions), clearChat) { clearChat = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        aiViewModel.clearLocalData(
                            clearTranslations = clearTranslations,
                            clearOcr = clearOcr,
                            clearMedia = clearMedia,
                            clearFlashcards = clearFlashcards,
                            clearExams = clearExams,
                            clearStudyUsage = clearUsage,
                            clearChatStats = clearChat
                        )
                        showClearDialog = false
                    },
                    enabled = clearTranslations || clearOcr || clearMedia || clearFlashcards || clearExams || clearUsage || clearChat
                ) { Text(stringResource(R.string.clear_selected)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun ClearCheckbox(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}
