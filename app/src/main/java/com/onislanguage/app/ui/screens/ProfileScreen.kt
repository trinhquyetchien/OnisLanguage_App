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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    var notifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var offlineMode by remember { mutableStateOf(true) }
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
        Box(
            modifier = Modifier
                .size(128.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(20.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)), CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AL", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.1f))
                    .background(Color.White, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Alex Learner", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text("alex@example.com", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .background(Color(0xFFFFF3E0), RoundedCornerShape(100.dp))
                .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(100.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFB8C00), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("ONIS PRO", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFFFB8C00))
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Learning Preferences
        SectionHeader("LEARNING PREFERENCES")
        SettingsCard {
            PreferenceRow(icon = Icons.Default.Language, label = "Target Language", value = "Japanese \uD83C\uDDEF\uD83C\uDDF5")
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            PreferenceRow(icon = Icons.Default.Public, label = "Native Language", value = "English \uD83C\uDDFA\uD83C\uDDF8")
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            PreferenceRow(icon = Icons.Default.AccessTime, label = "Daily Goal", value = "20 minutes / day")
        }
        Spacer(modifier = Modifier.height(32.dp))

        // App Settings
        SectionHeader("APP SETTINGS")
        SettingsCard {
            SettingsRow(icon = Icons.Default.Notifications, label = "Notifications", type = "toggle", checked = notifications, onChanged = { notifications = it }, iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), iconColor = MaterialTheme.colorScheme.primary)
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            SettingsRow(icon = Icons.Default.DarkMode, label = "Dark Mode", type = "toggle", checked = darkMode, onChanged = { darkMode = it }, iconBg = MaterialTheme.colorScheme.surfaceVariant, iconColor = MaterialTheme.colorScheme.onSurfaceVariant)
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            SettingsRow(icon = Icons.Default.CloudDownload, label = "Offline Mode", type = "toggle", checked = offlineMode, onChanged = { offlineMode = it }, iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), iconColor = MaterialTheme.colorScheme.primary)
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            SettingsRow(icon = Icons.Default.CreditCard, label = "Pro Plan (Active)", type = "link", iconBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), iconColor = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Support
        SettingsCard {
            SettingsRow(icon = Icons.Default.Help, label = "Help Center", type = "link", iconColor = MaterialTheme.colorScheme.onSurfaceVariant)
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            SettingsRow(icon = Icons.Default.Security, label = "Privacy Policy", type = "link", iconColor = MaterialTheme.colorScheme.onSurfaceVariant)
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            SettingsRow(
                icon = Icons.Default.ExitToApp, 
                label = "Log Out", 
                type = "button", 
                iconColor = MaterialTheme.colorScheme.error, 
                textColor = MaterialTheme.colorScheme.error,
                onClick = { onNavigate(com.onislanguage.app.navigation.Screen.Welcome.route) }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text("ONISLANGUAGE V1.0.2", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 4.dp)) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
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
fun PreferenceRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsRow(
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
        if (iconBg != null) {
            Box(
                modifier = Modifier.size(40.dp).background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(20.dp))
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor ?: MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        
        if (type == "toggle" && checked != null && onChanged != null) {
            Switch(
                checked = checked,
                onCheckedChange = onChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        } else if (type == "link") {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}
