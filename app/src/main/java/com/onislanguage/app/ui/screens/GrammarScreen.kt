package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GrammarScreen(onNavigate: (String) -> Unit) {
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
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 128.dp)
        ) {
            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = 100.dp, y = (-50).dp)
                        .size(128.dp)
                        .shadow(40.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                )
                Column(horizontalAlignment = Alignment.Start) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("LEVEL: JLPT N3 - N4", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("MOST FREQUENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Conditional form (~たら)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Grammar List
            GrammarCard(title = "~たら", tag = "Condition/If", desc = "Expresses a condition. If [A] happens, then [B] will happen.", example = "雨が降ったら、行きません。", exTrans = "If it rains, I won't go.", note = "Used for hypothetical situations or consequences.", colorType = "secondary")
            Spacer(modifier = Modifier.height(16.dp))
            GrammarCard(title = "~なければなりません", tag = "Obligation/Must", desc = "Indicates an obligation or something that must be done.", example = "明日、早く起きなければなりません。", exTrans = "I must wake up early tomorrow.", note = "Formal expression. Often shortened to ~なきゃ in speech.", colorType = "error")
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
        ) {
            Button(
                onClick = { onNavigate(com.onislanguage.app.navigation.Screen.Quiz.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Generate Grammar Quiz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GrammarCard(title: String, tag: String, desc: String, example: String, exTrans: String, note: String, colorType: String) {
    val tagBg = if (colorType == "secondary") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    val tagColor = if (colorType == "secondary") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(tagBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tagColor)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(desc, fontSize = 14.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(example, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exTrans, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append("Usage note: ")
                            }
                            append(note)
                        },
                        fontSize = 12.sp,
                        lineHeight = 16.8.sp, // 12 * 1.4
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("Save to Notes", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("Practice", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
