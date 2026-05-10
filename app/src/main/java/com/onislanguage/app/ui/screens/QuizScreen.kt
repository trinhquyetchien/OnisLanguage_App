package com.onislanguage.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
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
import com.onislanguage.app.ui.viewmodel.AuthViewModel
import com.onislanguage.app.ui.viewmodel.PracticeViewModel
import com.onislanguage.app.ui.components.AuthPromptView

private enum class ExamTab(val labelRes: Int) {
    Server(R.string.exam_library),
    MyExams(R.string.my_exams)
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
    var selectedTab by remember { mutableStateOf(ExamTab.Server) }
    var query by remember { mutableStateOf("") }
    
    val exams by viewModel.exams.collectAsState()
    val currentExam by viewModel.currentExam.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(selectedTab) {
        if (selectedTab == ExamTab.Server && authState != null) {
            viewModel.loadExams()
        }
    }

    if (submissionResult != null) {
        QuizResultView(result = submissionResult!!, onFinish = { viewModel.finishExam() })
    } else if (currentExam != null) {
        QuizTakingView(exam = currentExam!!, onSubmit = { viewModel.submitExam(currentExam!!.exam_id, it) })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            Text(stringResource(R.string.quiz), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(stringResource(R.string.home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = if (selectedTab == ExamTab.Server) 0 else 1,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == ExamTab.Server) 0 else 1]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(selected = selectedTab == ExamTab.Server, onClick = { selectedTab = ExamTab.Server }) {
                    Text(stringResource(ExamTab.Server.labelRes), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == ExamTab.MyExams, onClick = { selectedTab = ExamTab.MyExams }) {
                    Text(stringResource(ExamTab.MyExams.labelRes), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == ExamTab.Server && authState == null) {
                AuthPromptView(onLoginClick = { onNavigate(Screen.Login.route) })
            } else if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_exam)) },
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                val filteredExams = if (selectedTab == ExamTab.Server) {
                    exams.filter { it.title.contains(query, true) }
                } else {
                    emptyList() // Placeholder for local exams
                }

                if (filteredExams.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có đề thi nào")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredExams) { exam ->
                            ExamListCardV2(exam, onStart = { viewModel.startExam(exam.exam_id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizTakingView(exam: PracticeExam, onSubmit: (Map<String, String>) -> Unit) {
    val answers = remember { mutableStateMapOf<String, String>() }
    var currentQIndex by remember { mutableIntStateOf(0) }
    val currentQuestion = exam.questions[currentQIndex]

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(exam.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${currentQIndex + 1}/${exam.questions.size}")
        }
        
        LinearProgressIndicator(
            progress = { (currentQIndex + 1).toFloat() / exam.questions.size },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(16.dp),
                            color = if (isSelected) Color.White else Color.Black,
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
            }
            
            if (currentQIndex < exam.questions.size - 1) {
                Button(onClick = { currentQIndex++ }, modifier = Modifier.weight(1f).height(56.dp)) {
                    Text(stringResource(R.string.next))
                }
            } else {
                Button(
                    onClick = { onSubmit(answers.toMap()) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Nộp bài")
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
            shape = CircleShape,
            color = if (result.score >= 50) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            modifier = Modifier.size(150.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.score.toInt()}%", fontSize = 36.sp, fontWeight = FontWeight.Black, color = if (result.score >= 50) Color(0xFF2E7D32) else Color.Red)
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
private fun ExamListCardV2(exam: PracticeExam, onStart: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exam.title, fontWeight = FontWeight.Black, maxLines = 1)
                    Text("${exam.topic} • ${exam.level.uppercase()} • ${exam.question_count} câu", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onStart, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.start_exam))
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
