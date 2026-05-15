package com.onislanguage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onislanguage.app.data.model.StudyUsageDaySummary
import com.onislanguage.app.data.model.StudyUsageFeatureCount
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.ui.viewmodel.AiViewModel
import kotlin.math.max

@Composable
fun HistoryScreen(
    onNavigate: (String) -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val scrollState = rememberScrollState()
    val overview by viewModel.studyUsageOverview.collectAsState()
    val transcriptionHistory by viewModel.transcriptionHistory.collectAsState()
    val ocrHistory by viewModel.ocrHistory.collectAsState()
    val translationHistory by viewModel.translationHistory.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshStudyUsageOverview()
        viewModel.loadTranscriptionHistory()
        viewModel.loadOcrHistory()
        viewModel.loadTranslationHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 128.dp)
    ) {
        HeaderSection("History", "Lưu theo ngày bạn đã dùng chức năng nào, bao nhiêu lần và nhịp học trong tuần.")
        Spacer(modifier = Modifier.height(24.dp))

        StudyUsageOverviewCard(
            thisWeekTotal = overview.thisWeekTotal,
            weeklyDeltaPercent = overview.weeklyDeltaPercent ?: 0,
            chartPoints = overview.chartPoints.map { it.label to it.totalCount }
        )
        Spacer(modifier = Modifier.height(24.dp))

        QuickHistoryRow(
            mediaCount = transcriptionHistory.size,
            ocrCount = ocrHistory.size,
            translationCount = translationHistory.size,
            onNavigate = onNavigate
        )
        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TỔNG HỢP HOẠT ĐỘNG", fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            OutlinedButton(onClick = { showClearDialog = true }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Xóa dữ liệu nội bộ")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val featureTotals = overview.days
            .flatMap { it.featureCounts }
            .groupBy { it.featureKey }
            .map { (_, counts) ->
                StudyUsageFeatureCount(
                    featureKey = counts.first().featureKey,
                    featureLabel = counts.first().featureLabel,
                    count = counts.sumOf { it.count }
                )
            }
            .sortedByDescending { it.count }

        if (featureTotals.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            ) {
                Text(
                    "Chưa có dữ liệu sử dụng. Hãy dùng OCR, dịch, phân tích văn bản, kanji hoặc transcript để bắt đầu ghi lịch sử.",
                    modifier = Modifier.padding(18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    featureTotals.forEach { item ->
                        StudyUsageFeatureRow(item)
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        var clearTranslations by remember { mutableStateOf(false) }
        var clearOcr by remember { mutableStateOf(false) }
        var clearMedia by remember { mutableStateOf(false) }
        var clearFlashcards by remember { mutableStateOf(false) }
        var clearExams by remember { mutableStateOf(false) }
        var clearUsage by remember { mutableStateOf(false) }
        var clearChat by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Xóa dữ liệu nội bộ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ClearOption("Lịch sử dịch", clearTranslations) { clearTranslations = it }
                    ClearOption("Lịch sử OCR ảnh", clearOcr) { clearOcr = it }
                    ClearOption("Lịch sử Audio/Video", clearMedia) { clearMedia = it }
                    ClearOption("Flashcards local", clearFlashcards) { clearFlashcards = it }
                    ClearOption("Đề thi local", clearExams) { clearExams = it }
                    ClearOption("Thống kê học tập", clearUsage) { clearUsage = it }
                    ClearOption("Gợi ý chatbot", clearChat) { clearChat = it }
                }
            },
            confirmButton = {
                Button(
                    enabled = clearTranslations || clearOcr || clearMedia || clearFlashcards || clearExams || clearUsage || clearChat,
                    onClick = {
                        viewModel.clearLocalData(
                            clearTranslations = clearTranslations,
                            clearOcr = clearOcr,
                            clearMedia = clearMedia,
                            clearFlashcards = clearFlashcards,
                            clearExams = clearExams,
                            clearStudyUsage = clearUsage,
                            clearChatStats = clearChat
                        )
                        showClearDialog = false
                    }
                ) { Text("Xóa đã chọn") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun ClearOption(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onChecked(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(label)
    }
}

@Composable
private fun StudyUsageOverviewCard(
    thisWeekTotal: Int,
    weeklyDeltaPercent: Int,
    chartPoints: List<Pair<String, Int>>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "LEARNING OVERVIEW (THIS WEEK)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$thisWeekTotal lượt", fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                }
                val deltaLabel = if (weeklyDeltaPercent > 0) "+$weeklyDeltaPercent%" else "$weeklyDeltaPercent%"
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "$deltaLabel VS LAST WEEK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        letterSpacing = 1.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            val maxCount = max(chartPoints.maxOfOrNull { it.second } ?: 1, 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                chartPoints.forEachIndexed { index, (label, value) ->
                    UsageBarChartBar(
                        day = label,
                        heightFactor = value.toFloat() / maxCount.toFloat(),
                        isActive = index == chartPoints.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageBarChartBar(day: String, heightFactor: Float, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(108.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f), RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFactor.coerceAtLeast(0.05f))
                    .background(
                        brush = if (isActive) {
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        } else {
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                        },
                        shape = RoundedCornerShape(100.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickHistoryRow(
    mediaCount: Int,
    ocrCount: Int,
    translationCount: Int,
    onNavigate: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickHistoryCard("Media", "$mediaCount mục", Icons.Default.GraphicEq, Modifier.weight(1f)) {
            onNavigate(Screen.AudioToText.route)
        }
        QuickHistoryCard("OCR", "$ocrCount ảnh", Icons.Default.ImageSearch, Modifier.weight(1f)) {
            onNavigate(Screen.ImageAnalysis.route)
        }
        QuickHistoryCard("Dịch", "$translationCount bản", Icons.Default.Translate, Modifier.weight(1f)) {
            onNavigate(Screen.Analysis.route)
        }
    }
}

@Composable
private fun QuickHistoryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StudyUsageDayCard(day: StudyUsageDaySummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(day.displayDate, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${day.totalCount} lượt sử dụng", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(14.dp))
            day.featureCounts.forEachIndexed { index, item ->
                StudyUsageFeatureRow(item)
                if (index != day.featureCounts.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun StudyUsageFeatureRow(item: StudyUsageFeatureCount) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = usageFeatureIcon(item.featureKey),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(item.featureLabel, fontWeight = FontWeight.SemiBold)
                Text(item.featureKey, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(100.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("${item.count} lần", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
        }
    }
}

private fun usageFeatureIcon(featureKey: String): ImageVector {
    return when (featureKey) {
        "ocr" -> Icons.Default.ImageSearch
        "audio_transcribe", "video_transcribe", "youtube_transcribe" -> Icons.Default.GraphicEq
        "translate_text" -> Icons.Default.Translate
        "analyze_text" -> Icons.Default.Analytics
        "kanji" -> Icons.Default.AutoStories
        else -> Icons.Default.Analytics
    }
}
