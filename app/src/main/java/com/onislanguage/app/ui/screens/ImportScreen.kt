package com.onislanguage.app.ui.screens

import androidx.compose.animation.animateColorAsState
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
import com.onislanguage.app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(onNavigate: (String) -> Unit) {
    var activeTab by remember { mutableStateOf("video") }
    var autoDetect by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp)
    ) {
        HeaderSection("Import Material", "Đưa video, audio hoặc hình ảnh vào luồng phân tích chung của Onis.")
        Spacer(modifier = Modifier.height(24.dp))
        // Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("video", "audio", "image").forEach { tab ->
                val isActive = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable { activeTab = tab }
                        .background(
                            if (isActive) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Upload Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(32.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Thêm $activeTab vào workspace",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "hoặc",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { },
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Browse files", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "MP4, MOV, AVI • tối đa 500MB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // URL Import
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Text(
                text = "OR IMPORT FROM URL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = { Text("Paste link", color = MaterialTheme.colorScheme.outline) },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.outline) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CloudButton(icon = Icons.Default.PlayArrow, label = "YouTube")
            Spacer(modifier = Modifier.width(8.dp))
            CloudButton(icon = Icons.Default.CloudQueue, label = "Google Drive")
            Spacer(modifier = Modifier.width(8.dp))
            CloudButton(icon = Icons.Default.Cloud, label = "Dropbox")
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Language Settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(30.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AUTO-DETECT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = autoDetect,
                            onCheckedChange = { autoDetect = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("SOURCE LANGUAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                LanguageSelector(emoji = "\uD83C\uDDEF\uD83C\uDDF5", language = "Japanese")
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("TRANSLATE TO", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                LanguageSelector(emoji = "\uD83C\uDDFA\uD83C\uDDF8", language = "English")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onNavigate(Screen.Analysis.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Start analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun CloudButton(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(100.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(100.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LanguageSelector(emoji: String, language: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(language, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}
