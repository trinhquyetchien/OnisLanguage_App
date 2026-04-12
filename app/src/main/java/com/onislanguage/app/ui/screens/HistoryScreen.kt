package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun HistoryScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var activeTab by remember { mutableStateOf("Materials") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 128.dp)
    ) {
        // Overview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("LEARNING OVERVIEW (THIS WEEK)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("4.5 hrs", fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("+12% VS LAST WEEK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer, letterSpacing = 1.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    BarChartBar(day = "M", heightFactor = 12f / 24f, isActive = false)
                    BarChartBar(day = "T", heightFactor = 20f / 24f, isActive = false)
                    BarChartBar(day = "W", heightFactor = 24f / 24f, isActive = true)
                    BarChartBar(day = "T", heightFactor = 16f / 24f, isActive = false)
                    BarChartBar(day = "F", heightFactor = 14f / 24f, isActive = false)
                    BarChartBar(day = "S", heightFactor = 8f / 24f, isActive = false)
                    BarChartBar(day = "S", heightFactor = 10f / 24f, isActive = false)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(6.dp)
        ) {
            listOf("Materials", "Vocab", "Quizzes").forEach { tab ->
                val isActive = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(if (isActive) 4.dp else 0.dp, RoundedCornerShape(12.dp), spotColor = if (isActive) Color.Black.copy(alpha = 0.05f) else Color.Transparent)
                        .background(if (isActive) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { activeTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // History List
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("RECENTLY ANALYZED", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("View all", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { })
        }
        Spacer(modifier = Modifier.height(16.dp))

        HistoryCard(title = "Japanese Street Food Interview", type = "Video", icon = Icons.Default.PlayCircle, time = "2 days ago", words = 128, grammar = 14, img = "https://picsum.photos/seed/food/200/200")
        Spacer(modifier = Modifier.height(16.dp))
        HistoryCard(title = "Tokyo Subway Map & Rules", type = "Image/OCR", icon = Icons.Default.CameraAlt, time = "5 days ago", words = 45, grammar = 2, img = "https://picsum.photos/seed/subway/200/200")
        Spacer(modifier = Modifier.height(16.dp))
        HistoryCard(title = "Podcast Ep #42 - Daily Commute", type = "Audio", icon = Icons.Default.Mic, time = "Last week", words = 310, grammar = 25, img = "https://picsum.photos/seed/podcast/200/200")
    }
}

@Composable
fun BarChartBar(day: String, heightFactor: Float, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFactor)
                    .background(
                        brush = if (isActive) Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)),
                        shape = RoundedCornerShape(100.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HistoryCard(title: String, type: String, icon: ImageVector, time: String, words: Int, grammar: Int, img: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(type, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("$words words", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("$grammar grammar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
