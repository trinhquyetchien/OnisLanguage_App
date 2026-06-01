package com.onislanguage.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class SystemToastType {
    Success,
    Error
}

@Composable
fun SystemToastHost(
    message: String?,
    type: SystemToastType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = 2800L
) {
    val visible = !message.isNullOrBlank()

    LaunchedEffect(message, type) {
        if (visible) {
            delay(autoDismissMillis)
            onDismiss()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 10.dp,
                shadowElevation = 8.dp,
                color = Color.Transparent
            ) {
                val accent = if (type == SystemToastType.Success) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                val icon = if (type == SystemToastType.Success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline
                val title = if (type == SystemToastType.Success) "Thông báo hệ thống" else "Có lỗi xảy ra"

                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    accent.copy(alpha = 0.16f),
                                    MaterialTheme.colorScheme.surface
                                )
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accent)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = message.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
