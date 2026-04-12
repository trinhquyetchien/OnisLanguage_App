package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onislanguage.app.navigation.Screen

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 120.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good morning, Alex \uD83D\uDC4B",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ready to learn today?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp, top = 10.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .align(Alignment.TopEnd)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = "https://picsum.photos/seed/user/100/100",
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Progress Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "TODAY'S PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "12 / 20 words",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "\uD83D\uDD25 5 day streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                LinearProgressIndicator(
                    progress = 0.6f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "8 words to reach your goal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions
        Text(
            text = "Import Content",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem(modifier = Modifier.weight(1f), icon = Icons.Filled.Videocam, title = "Upload Video", iconColor = Color(0xFF1E88E5), bgColor = Color(0xFFE3F2FD), onClick = { onNavigate(Screen.Import.route) })
            QuickActionItem(modifier = Modifier.weight(1f), icon = Icons.Filled.Mic, title = "Upload Audio", iconColor = Color(0xFF8E24AA), bgColor = Color(0xFFF3E5F5), onClick = { onNavigate(Screen.Import.route) })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem(modifier = Modifier.weight(1f), icon = Icons.Filled.Image, title = "Upload Image", iconColor = Color(0xFF00897B), bgColor = Color(0xFFE0F2F1), onClick = { onNavigate(Screen.Import.route) })
            QuickActionItem(modifier = Modifier.weight(1f), icon = Icons.Filled.Link, title = "Import URL", iconColor = Color(0xFFFB8C00), bgColor = Color(0xFFFFF3E0), onClick = { onNavigate(Screen.Import.route) })
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Recent Materials
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Recent Materials",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "See all",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigate(Screen.History.route) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                RecentMaterialItem("BBC News: Climate Change", 15, 3, "2 hours ago", "\uD83C\uDDEF\uD83C\uDDF5", "https://picsum.photos/seed/news/400/200")
            }
            item {
                RecentMaterialItem("Spanish Vlogs: Madrid Life", 24, 5, "Yesterday", "\uD83C\uDDEA\uD83C\uDDF8", "https://picsum.photos/seed/vlog/400/200")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // AI Suggestion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI TUTOR SUGGESTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"Try describing your breakfast today in Japanese using the past tense.\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 19.6.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(modifier: Modifier = Modifier, icon: ImageVector, title: String, iconColor: Color, bgColor: Color, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.02f))
            .clickable { onClick() }
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun RecentMaterialItem(title: String, words: Int, grammar: Int, time: String, lang: String, imgUrl: String) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.02f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column {
            Box(modifier = Modifier.height(120.dp).fillMaxWidth()) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
                Text(
                    text = lang,
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 16.8.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    ItemTag("$words words")
                    Spacer(modifier = Modifier.width(8.dp))
                    ItemTag("$grammar grammar")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = time,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ItemTag(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
