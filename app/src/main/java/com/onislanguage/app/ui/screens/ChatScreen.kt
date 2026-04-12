package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onislanguage.app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = "https://picsum.photos/seed/akira/100/100",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Akira \uD83E\uDD16", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047), letterSpacing = 1.sp)
                    }
                }
                OutlinedButton(
                    onClick = { onNavigate(Screen.Dashboard.route) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("End Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                // Topic Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CURRENT TOPIC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                            Text("Ordering food at a restaurant", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    }
                }

                // Chat Area
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 128.dp)
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text("TODAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, letterSpacing = 2.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        ChatBubble(isAi = true, message = "こんにちは！今日はレストランでの注文の練習をしましょう。準備はいいですか？")
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    item {
                        ChatBubble(isAi = false, message = "はい、準備ができました。", time = "10:42 AM")
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        ChatBubble(isAi = true, message = "素晴らしい！いらっしゃいませ。何名様ですか？")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Grammar tip
                        Row(
                            modifier = Modifier
                                .padding(end = 48.dp)
                                .background(Color(0xFFFFF8E1), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFFFE082).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("GRAMMAR TIP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    buildAnnotatedString {
                                        withStyle(SpanStyle(fontSize = 12.sp, color = Color(0xFFE65100).copy(alpha = 0.8f))) {
                                            append("For saying 'I am ready', you can also say ")
                                        }
                                        withStyle(SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))) {
                                            append("準備万端です")
                                        }
                                        withStyle(SpanStyle(fontSize = 12.sp, color = Color(0xFFE65100).copy(alpha = 0.8f))) {
                                            append(" (Junbi bantan desu) to sound more natural.")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Input Area
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    .padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val prompts = listOf("Can I see the menu?", "A table for two", "Excuse me", "Water, please")
                        items(prompts.size) { i ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(prompts[i], fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                            .shadow(4.dp, RoundedCornerShape(100.dp), spotColor = Color.Black.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.KeyboardAlt, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type your message...", color = MaterialTheme.colorScheme.outline) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowCircleUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(isAi: Boolean, message: String, time: String? = null) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isAi) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Column(horizontalAlignment = if (isAi) Alignment.Start else Alignment.End) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .shadow(10.dp, RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = if (isAi) 0.dp else 24.dp,
                        bottomEnd = if (isAi) 24.dp else 0.dp
                    ), spotColor = if (isAi) Color.Black.copy(alpha = 0.02f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .background(
                        brush = if (isAi) Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = if (isAi) 0.dp else 24.dp,
                            bottomEnd = if (isAi) 24.dp else 0.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    message,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isAi) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Show translation", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                }
            } else if (time != null) {
                Text(time, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}
