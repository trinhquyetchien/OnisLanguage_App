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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onislanguage.app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(onNavigate: (String) -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vocabulary Quiz", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("QUESTION 3 OF 10", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Dashboard.route) }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\uD83D\uDD25", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 128.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = 0.3f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "What is the correct translation for the underlined word?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Question Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .absoluteOffset(x = 100.dp, y = (-70).dp)
                            .size(100.dp)
                            .shadow(40.dp, CircleShape, spotColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), CircleShape)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp)) {
                                    append("彼女は")
                                }
                                withStyle(SpanStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp, textDecoration = TextDecoration.Underline)) {
                                    append("図書館")
                                }
                                withStyle(SpanStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp)) {
                                    append("で本を読みます。")
                                }
                            },
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Kanojo wa toshokan de hon o yomimasu.",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Options
                val options = listOf(
                    "A" to "Restaurant",
                    "B" to "Library",
                    "C" to "School",
                    "D" to "Hospital"
                )

                options.forEach { (id, label) ->
                    val isSelected = selected == id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .clickable { selected = id }
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(id, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.outline)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                fontSize = 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Need a hint?", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Bottom Action Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), spotColor = Color.Black.copy(alpha = 0.12f))
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(top = 16.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("HINT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        enabled = selected != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Check Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("SKIP", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}
