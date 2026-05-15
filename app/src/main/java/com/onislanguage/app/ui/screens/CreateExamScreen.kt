package com.onislanguage.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.R
import com.onislanguage.app.data.model.*
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.ui.components.SystemToastHost
import com.onislanguage.app.ui.components.SystemToastType
import com.onislanguage.app.ui.viewmodel.PracticeViewModel
import com.onislanguage.app.utils.UriFileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.providePracticeViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("N3") }
    var formError by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val questions = remember { mutableStateListOf<QuestionDraft>() }
    val fileImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val displayName = UriFileUtils.getDisplayName(context, uri)?.substringBeforeLast(".")?.ifBlank { "Đề AI từ file" } ?: "Đề AI từ file"
        val content = UriFileUtils.extractTextFromUri(context, uri)
        if (title.isBlank()) {
            title = displayName
        }
        formError = if (content.isBlank()) "File không có nội dung để tạo đề thi." else null
        if (content.isNotBlank()) {
            viewModel.generateExamFromText(title.ifBlank { displayName }, content, level)
        }
    }

    LaunchedEffect(notice) {
        if (!notice.isNullOrBlank() && notice!!.contains("đề thi", ignoreCase = true)) {
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.create_exam), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val builtQuestions = questions.mapIndexedNotNull { index, draft ->
                                if (draft.prompt.isBlank()) return@mapIndexedNotNull null
                                val normalizedOptions = draft.options.map { it.trim() }
                                val validOptions = normalizedOptions.filter { it.isNotBlank() }
                                val correctAnswer = draft.correctAnswerIndex
                                    .takeIf { it in normalizedOptions.indices }
                                    ?.let { normalizedOptions[it] }
                                    ?.takeIf { it.isNotBlank() }

                                PracticeQuestion(
                                    question_id = "manual_${System.currentTimeMillis()}_$index",
                                    kind = "multiple_choice",
                                    prompt = draft.prompt,
                                    imageUrl = draft.imageUrl.takeIf { it.isNotBlank() },
                                    audioUrl = draft.audioUrl.takeIf { it.isNotBlank() },
                                    options = validOptions,
                                    position = index + 1,
                                    correct_answer = correctAnswer
                                )
                            }
                            val validationMessage = when {
                                title.isBlank() -> "Nhập tên đề thi."
                                builtQuestions.isEmpty() -> "Thêm ít nhất 1 câu hỏi hợp lệ."
                                builtQuestions.any { it.options.size < 2 } -> "Mỗi câu hỏi cần ít nhất 2 đáp án."
                                builtQuestions.any { it.correct_answer.isNullOrBlank() } -> "Chọn đáp án đúng cho từng câu hỏi."
                                else -> null
                            }

                            if (validationMessage != null) {
                                formError = validationMessage
                            } else {
                                formError = null
                                viewModel.saveLocalExam(title, level, builtQuestions, onSaved = onBack)
                            }
                        }) {
                            Icon(Icons.Default.Save, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (topic.isNotEmpty()) {
                                formError = null
                                viewModel.generateAIExam(topic, 5) { aiQuestions ->
                                    questions.clear()
                                    aiQuestions.forEach { q ->
                                        questions.add(QuestionDraft().apply {
                                            prompt = q.prompt
                                            options = q.options.toMutableStateList()
                                            correctAnswerIndex = q.options.indexOf(q.correct_answer).takeIf { it >= 0 } ?: 0
                                        })
                                    }
                                }
                            } else {
                                formError = "Nhập chủ đề để AI tạo đề."
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.AutoAwesome, null)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SmallFloatingActionButton(
                        onClick = { fileImportLauncher.launch("*/*") },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Icon(Icons.Default.UploadFile, null)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ExtendedFloatingActionButton(
                        onClick = { questions.add(QuestionDraft()) },
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text(stringResource(R.string.add_question)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Thông tin chung", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                        if (!formError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(formError ?: "", color = MaterialTheme.colorScheme.error)
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                formError = null
                            },
                            label = { Text(stringResource(R.string.exam_title)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = topic,
                                onValueChange = {
                                    topic = it
                                    formError = null
                                },
                                label = { Text("Chủ đề (để AI tạo đề)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = level,
                                onValueChange = {
                                    level = it
                                    formError = null
                                },
                                label = { Text(stringResource(R.string.exam_level)) },
                                modifier = Modifier.width(100.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Danh sách câu hỏi (${questions.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    itemsIndexed(questions) { index, question ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Câu hỏi ${index + 1}", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { questions.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = question.prompt,
                                    onValueChange = {
                                        question.prompt = it
                                        formError = null
                                    },
                                    label = { Text(stringResource(R.string.question_prompt)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Đính kèm ảnh/audio (Option)", style = MaterialTheme.typography.labelSmall)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Các lựa chọn (5 đáp án)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                                question.options.forEachIndexed { optIndex, opt ->
                                    OutlinedTextField(
                                        value = opt,
                                        onValueChange = {
                                            question.options[optIndex] = it
                                            formError = null
                                        },
                                        label = { Text("Đáp án ${'A' + optIndex}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            RadioButton(
                                                selected = question.correctAnswerIndex == optIndex,
                                                onClick = { question.correctAnswerIndex = optIndex }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        SystemToastHost(
            message = notice,
            type = SystemToastType.Success,
            onDismiss = viewModel::clearFeedback,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        SystemToastHost(
            message = error,
            type = SystemToastType.Error,
            onDismiss = viewModel::clearFeedback,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

class QuestionDraft {
    var prompt by mutableStateOf("")
    var imageUrl by mutableStateOf("")
    var audioUrl by mutableStateOf("")
    var options = mutableStateListOf("", "", "", "", "")
    var correctAnswerIndex by mutableIntStateOf(-1)
}
