package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VocabularyScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 128.dp)
        ) {
            HeaderSection("Vocabulary", "Từ mới, ví dụ và tín hiệu độ khó được gom về một bảng tra cứu gọn hơn.")
            Spacer(modifier = Modifier.height(24.dp))
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard(modifier = Modifier.weight(1f), label = "43 New", color = MaterialTheme.colorScheme.primary, bg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), progress = 0.8f, sub = "NEW")
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(modifier = Modifier.weight(1f), label = "3 Known", color = Color(0xFF43A047), bg = Color(0xFFE8F5E9), progress = 0.25f, sub = "KNOWN")
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(modifier = Modifier.weight(1f), label = "1 Difficult", color = MaterialTheme.colorScheme.error, bg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), progress = 0.33f, sub = "HARD")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Search
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp)),
                placeholder = { Text("Search vocabulary...", color = MaterialTheme.colorScheme.outline) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val chips = listOf("All", "Nouns", "Verbs", "Adjectives", "Phrases", "N5", "N4", "N3")
                items(chips.size) { i ->
                    val isActive = i == 0
                    Box(
                        modifier = Modifier
                            .shadow(if (isActive) 8.dp else 0.dp, RoundedCornerShape(100.dp), spotColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
                            .background(
                                brush = if (isActive) Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.tertiaryContainer))
                                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .border(1.dp, if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            chips[i],
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // List
            VocabCard(kanji = "日本語", kana = "にほんご", meaning = "Japanese language", type = "Noun", level = "N3", tag = "High frequency", example = "私は日本語を勉強しています。", exTrans = "I am studying Japanese.", colorType = "primary")
            Spacer(modifier = Modifier.height(16.dp))
            VocabCard(kanji = "食べる", kana = "たべる", meaning = "to eat", type = "Verb", level = "N5", tag = null, example = "林檎を食べる。", exTrans = "Eat an apple.", colorType = "emerald")
            Spacer(modifier = Modifier.height(16.dp))
            VocabCard(kanji = "美しい", kana = "うつくしい", meaning = "beautiful", type = "Adjective", level = "N4", tag = null, example = "美しい花。", exTrans = "Beautiful flower.", colorType = "secondary")
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
                .shadow(10.dp, RoundedCornerShape(100.dp), spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(100.dp))
                .clickable { }
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add all to Flashcards", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, color: Color, bg: Color, progress: Float, sub: String) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(sub, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = color)
        }
    }
}

@Composable
fun VocabCard(kanji: String, kana: String, meaning: String, type: String, level: String, tag: String?, example: String, exTrans: String, colorType: String) {
    val mainColor = when (colorType) {
        "primary" -> MaterialTheme.colorScheme.primary
        "emerald" -> Color(0xFF43A047)
        else -> MaterialTheme.colorScheme.secondary
    }
    val bgColor = when (colorType) {
        "primary" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        "emerald" -> Color(0xFFE8F5E9)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(kanji, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFECB3), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(level, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
                    }
                    if (tag != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE0F2F1), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                        }
                    }
                }
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(kana, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(bgColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mainColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(meaning, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .border(androidx.compose.foundation.BorderStroke(width = 4.dp, color = mainColor.copy(alpha = 0.5f))) // approximate left border
                    .padding(12.dp)
            ) {
                Column {
                    Text(example, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exTrans, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
