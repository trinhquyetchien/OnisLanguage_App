package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.data.api.FuriganaTokenDto
import com.onislanguage.app.data.api.JapaneseTextDisplayDto
import com.onislanguage.app.data.api.KanjiItemDto
import com.onislanguage.app.data.api.VocabularyItemDto
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.ui.viewmodel.AiViewModel
import com.onislanguage.app.ui.viewmodel.FlashcardViewModel

@Composable
fun AnalysisScreen(
    onNavigate: (String) -> Unit,
    aiViewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    }),
    flashcardViewModel: FlashcardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideFlashcardViewModel() as T
        }
    })
) {
    var input by remember { mutableStateOf("日本語を勉強しています。今日は文法と単語を確認したいです。") }
    val analysisResult by aiViewModel.textAnalysisResult.collectAsState()
    val isLoading by aiViewModel.isLanguageLoading.collectAsState()
    val languageError by aiViewModel.languageError.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        aiViewModel.clearTextAnalysis()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 128.dp)
    ) {
        Text("Phân tích văn bản", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            shape = RoundedCornerShape(20.dp),
            placeholder = { Text("Paste văn bản Nhật hoặc Việt vào đây...") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val sourceLanguage = if (containsJapaneseForMobile(input)) "ja" else "vi"
                aiViewModel.analyzeText(input, sourceLanguage)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Phân tích")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        languageError?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        analysisResult?.let { result ->
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle("Theo từng câu")
            Spacer(modifier = Modifier.height(10.dp))
            result.sentences.forEach { sentence ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        AnalysisJapaneseTextDisplay(sentence.text_display)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionTitle("Từ vựng")
                Button(
                    onClick = {
                        flashcardViewModel.addAnalysisItemsToDeck(
                            vocabulary = result.analysis.vocabulary,
                            kanji = result.kanji
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Tạo bộ flashcard")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (result.analysis.vocabulary.isEmpty()) {
                EmptyAnalysisText()
            } else {
                result.analysis.vocabulary.forEach { item ->
                    VocabularyAnalysisRow(item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle("Ngữ pháp")
            Spacer(modifier = Modifier.height(10.dp))
            if (result.analysis.grammar_points.isEmpty()) {
                EmptyAnalysisText()
            } else {
                result.analysis.grammar_points.forEach { item ->
                    AnalysisInfoCard(
                        title = item.pattern,
                        content = item.explanation_vi,
                        trailing = item.example_ja
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle("Kanji")
            Spacer(modifier = Modifier.height(10.dp))
            if (result.kanji.isEmpty()) {
                EmptyAnalysisText()
            } else {
                result.kanji.forEach { item ->
                    KanjiAnalysisRow(item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyAnalysisText() {
    Text("Không có dữ liệu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun VocabularyAnalysisRow(item: VocabularyItemDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.surface, fontWeight = FontWeight.Bold)
                item.reading?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.meaning_vi, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnalysisJapaneseTextDisplay(display: JapaneseTextDisplayDto) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AnalysisFuriganaText(tokens = display.tokens, fallbackText = display.text)
        display.translation_vi?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisFuriganaText(
    tokens: List<FuriganaTokenDto>,
    fallbackText: String
) {
    if (tokens.isEmpty()) {
        Text(text = fallbackText, fontWeight = FontWeight.Bold)
        return
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tokens.forEach { token ->
            AnalysisFuriganaToken(token)
        }
    }
}

@Composable
private fun AnalysisFuriganaToken(token: FuriganaTokenDto) {
    val readingSlotHeight = 12.dp
    Column(
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Box(
            modifier = Modifier.height(readingSlotHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            token.reading?.takeIf { it.isNotBlank() }?.let { reading ->
                Text(
                    text = reading,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = token.surface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KanjiAnalysisRow(item: KanjiItemDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(item.kanji, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Column {
                item.reading?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                item.meaning_vi?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun AnalysisInfoCard(title: String, content: String, trailing: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TextSnippet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, color = MaterialTheme.colorScheme.onSurfaceVariant)
            trailing?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun containsJapaneseForMobile(text: String): Boolean {
    return text.any { ch ->
        ch in '\u3040'..'\u30ff' || ch in '\u3400'..'\u9fff'
    }
}
