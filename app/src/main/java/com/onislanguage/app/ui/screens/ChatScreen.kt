package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.data.model.ChatSession
import com.onislanguage.app.data.model.ChatUiMessage
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.ui.viewmodel.AiViewModel

private val defaultQuickChatPrompts = listOf(
    "Giải thích mẫu ngữ pháp 〜てしまう",
    "Cho ví dụ hội thoại ở nhà hàng",
    "Phân biệt は và が",
    "Sửa câu tiếng Nhật này giúp tôi"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigate: (String) -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val sessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentChatSessionId.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val draft by viewModel.chatDraft.collectAsState()
    val isSending by viewModel.isChatSending.collectAsState()
    val chatError by viewModel.chatError.collectAsState()
    val topPrompts by viewModel.topChatPrompts.collectAsState()
    val listState = rememberLazyListState()
    val quickChatPrompts = if (topPrompts.isNotEmpty()) topPrompts else defaultQuickChatPrompts

    LaunchedEffect(messages.size, isSending) {
        val extraItem = if (isSending) 1 else 0
        val targetIndex = messages.lastIndex + extraItem
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = null,
                                modifier = Modifier.padding(10.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Akira", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalIconButton(onClick = { viewModel.createNewChatSession() }) {
                            Icon(Icons.Default.Add, contentDescription = "New session")
                        }
                        FilledTonalIconButton(onClick = { viewModel.resetChat() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset chat")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sessions, key = { it.id }) { session ->
                        ChatSessionChip(
                            session = session,
                            selected = session.id == currentSessionId,
                            onClick = { viewModel.selectChatSession(session.id) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickChatPrompts) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { viewModel.sendChatMessage(prompt) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(prompt, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = viewModel::updateChatDraft,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        placeholder = { Text("Nhập câu hỏi hoặc đoạn hội thoại tiếng Nhật...") },
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendChatMessage() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    FilledTonalIconButton(
                        onClick = { viewModel.sendChatMessage() },
                        modifier = Modifier.size(54.dp),
                        enabled = draft.isNotBlank() && !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatError != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    ) {
                        Text(
                            chatError ?: "",
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                ChatMessageBubble(message = message)
            }

            if (isSending) {
                item(key = "typing") {
                    TypingBubble()
                }
            }
        }
    }
}

@Composable
private fun ChatSessionChip(
    session: ChatSession,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = session.title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ChatMessageBubble(message: ChatUiMessage) {
    val isAssistant = message.role == "assistant"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isAssistant) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (isAssistant) 8.dp else 24.dp,
                bottomEnd = if (isAssistant) 24.dp else 8.dp
            ),
            color = if (isAssistant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (isAssistant) 0.9f else 0.82f)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    if (isAssistant) "Akira" else "Bạn",
                    color = if (isAssistant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    message.content,
                    color = if (isAssistant) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun TypingBubble() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Đang trả lời", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
