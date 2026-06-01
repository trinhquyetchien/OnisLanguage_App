package com.onislanguage.app.ui.screens

import androidx.compose.animation.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.R
import com.onislanguage.app.data.model.PracticeExam
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.ui.components.SystemToastHost
import com.onislanguage.app.ui.components.SystemToastType
import com.onislanguage.app.ui.viewmodel.AuthViewModel
import com.onislanguage.app.ui.viewmodel.PracticeViewModel
import com.onislanguage.app.ui.components.AuthPromptView
import com.onislanguage.app.utils.UriFileUtils

private enum class ExamTab(val labelRes: Int) {
    MyExams(R.string.my_exams),
    Server(R.string.exam_library)
}

@Composable
fun QuizScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: PracticeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.providePracticeViewModel() as T
        }
    }),
    authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ExamTab.MyExams) }
    var query by remember { mutableStateOf("") }
    
    val exams by viewModel.exams.collectAsState()
    val localExams by viewModel.localExams.collectAsState()
    val currentExam by viewModel.currentExam.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val error by viewModel.error.collectAsState()

    val fileImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val displayName = UriFileUtils.getDisplayName(context, uri)?.substringBeforeLast(".")?.ifBlank { "Đề AI từ file" } ?: "Đề AI từ file"
        val content = UriFileUtils.extractTextFromUri(context, uri)
        viewModel.generateExamFromText(displayName, content)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == ExamTab.Server && authState != null) {
            viewModel.loadExams()
        } else if (selectedTab == ExamTab.MyExams) {
            viewModel.loadLocalExams()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    if (submissionResult != null) {
        QuizResultView(result = submissionResult!!, onFinish = { viewModel.finishExam() })
    } else if (currentExam != null) {
        val isLocalExam = currentExam!!.tags.contains("local")
        QuizTakingView(
            exam = currentExam!!,
            isLocal = isLocalExam,
            onSubmit = { viewModel.submitExam(currentExam!!.exam_id, it, isLocalExam) }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(20.dp)
        ) {
            Text(stringResource(R.string.quiz), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            
            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == ExamTab.Server && authState == null) {
                AuthPromptView(onLoginClick = { onNavigate(Screen.Login.route) })
            } else if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(4.dp)
                        ) {
                            val isMyExams = selectedTab == ExamTab.MyExams
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isMyExams) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = ExamTab.MyExams }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(ExamTab.MyExams.labelRes),
                                    color = if (isMyExams) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            val isServer = selectedTab == ExamTab.Server
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isServer) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = ExamTab.Server }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(ExamTab.Server.labelRes),
                                    color = if (isServer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (selectedTab == ExamTab.MyExams) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onNavigate(Screen.CreateExam.route) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.create_exam), fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { fileImportLauncher.launch("*/*") },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.ai_from_file), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.search_exam)) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val filteredExams = if (selectedTab == ExamTab.Server) {
                    exams.filter { it.title.contains(query, true) }
                } else {
                    localExams.filter { it.title.contains(query, true) }
                }

                if (filteredExams.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (selectedTab == ExamTab.Server) stringResource(R.string.no_server_exams)
                            else stringResource(R.string.no_personal_exams)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredExams) { exam ->
                            ExamListCardV2(
                                exam = exam,
                                isLocalExam = selectedTab == ExamTab.MyExams,
                                onPrimaryAction = {
                                    if (selectedTab == ExamTab.MyExams) {
                                        viewModel.startExam(exam.exam_id, true)
                                    } else {
                                        viewModel.downloadExam(exam.exam_id)
                                    }
                                }
                            )
                        }
                    }
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

@Composable
fun QuizTakingView(exam: PracticeExam, isLocal: Boolean, onSubmit: (Map<String, String>) -> Unit) {
    val answers = remember { mutableStateMapOf<String, String>() }
    var currentQIndex by remember { mutableIntStateOf(0) }
    val currentQuestion = exam.questions[currentQIndex]

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exam.title, fontWeight = FontWeight.Bold)
                Text(
                    if (isLocal) "${stringResource(R.string.personal_exam)} • ${exam.level}" else "${exam.topic} • ${exam.level}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("${currentQIndex + 1}/${exam.questions.size}")
        }
        
        LinearProgressIndicator(
            progress = { (currentQIndex + 1).toFloat() / exam.questions.size },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(exam.questions) { index, question ->
                val answered = answers.containsKey(question.question_id)
                FilterChip(
                    selected = currentQIndex == index,
                    onClick = { currentQIndex = index },
                    label = { Text("${index + 1}") },
                    leadingIcon = if (answered) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(currentQuestion.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))
                
                currentQuestion.options.forEach { option ->
                    val isSelected = answers[currentQuestion.question_id] == option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { answers[currentQuestion.question_id] = option },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (currentQIndex > 0) {
                OutlinedButton(onClick = { currentQIndex-- }, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.previous))
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            if (currentQIndex < exam.questions.size - 1) {
                Button(onClick = { currentQIndex++ }, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.next))
                }
            } else {
                Button(
                    onClick = { onSubmit(answers.toMap()) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = answers.size == exam.questions.size,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(stringResource(R.string.submit_exam))
                }
            }
        }
    }
}

@Composable
fun QuizResultView(result: com.onislanguage.app.data.model.PracticeSubmissionResponse, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Kết quả bài thi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = if (result.score >= 50) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(150.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.score.toInt()}%", fontSize = 36.sp, fontWeight = FontWeight.Black, color = if (result.score >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Text("${result.correct_answers}/${result.total_questions}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        
        Spacer(Modifier.height(40.dp))
        
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Hoàn thành")
        }
    }
}

@Composable
private fun ExamListCardV2(
    exam: PracticeExam,
    isLocalExam: Boolean,
    onPrimaryAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exam.title, fontWeight = FontWeight.Black, maxLines = 1)
                    val displayCount = exam.question_count.takeIf { it > 0 } ?: exam.questions.size
                    Text("${exam.topic} • ${exam.level.uppercase()} • ${displayCount} câu", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onPrimaryAction, shape = RoundedCornerShape(12.dp)) {
                    Icon(
                        if (isLocalExam) Icons.Default.PlayArrow else Icons.Default.Download,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isLocalExam) stringResource(R.string.start_exam) else stringResource(R.string.download_exam))
                }
            }
            if (exam.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(exam.tags) { tag ->
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
