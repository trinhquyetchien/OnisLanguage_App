package com.onislanguage.app.ui.screens

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
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onislanguage.app.navigation.Screen

@Composable
fun AnalysisScreen(onNavigate: (String) -> Unit) {
    var view by remember { mutableStateOf("original") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 128.dp)
    ) {
        // Media Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.02f))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            AsyncImage(
                model = "https://picsum.photos/seed/tokyo/200/140",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("00:45 / 3:42", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = 0.2f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Toggle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                .padding(4.dp)
        ) {
            Row {
                ToggleBtn(label = "Original", isActive = view == "original") { view = "original" }
                ToggleBtn(label = "Translation", isActive = view == "translation") { view = "translation" }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Transcript Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Transcript", fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("47", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Text("\uD83C\uDDEF\uD83C\uDDF5", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Transcript items
        TranscriptCard(
            text = "日本は世界で最も急速に高齢化が進んでいる国の一つです。",
            translation = "Japan is one of the world's most rapidly aging countries.",
            tag = "Noun Phrase Structure"
        )
        Spacer(modifier = Modifier.height(16.dp))
        TranscriptCard(
            text = "この現象は、経済に大きな影響を与えています。",
            translation = "This phenomenon is having a significant impact on the economy.",
            tag = "Causal Relationship"
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Action Bar
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionBtn(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MenuBook,
                label = "Vocab Analysis",
                iconColor = MaterialTheme.colorScheme.primary,
                onTap = { onNavigate(Screen.Vocabulary.route) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionBtn(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Description,
                label = "Grammar Analysis",
                iconColor = MaterialTheme.colorScheme.tertiary,
                onTap = { onNavigate(Screen.Grammar.route) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(15.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(Screen.Chat.route) }
                    .background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "PRACTICE AI",
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleBtn(label: String, isActive: Boolean, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onTap() }
            .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(100.dp))
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TranscriptCard(text: String, translation: String, tag: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(12.dp)
                .shadow(10.dp, CircleShape, spotColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f), CircleShape)
        )
        Column {
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 27.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Text(translation, fontSize = 14.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tag.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD TO DECK", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ActionBtn(modifier: Modifier = Modifier, icon: ImageVector, label: String, iconColor: Color, onTap: () -> Unit) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.02f))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTap() }
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label.uppercase(), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
