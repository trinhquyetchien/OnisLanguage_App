package com.onislanguage.app.ui.screens
import com.onislanguage.app.utils.UriFileUtils

import android.Manifest
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.VideoView
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Image
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import com.onislanguage.app.R
import com.onislanguage.app.data.api.FuriganaTokenDto
import com.onislanguage.app.data.api.JapaneseTextDisplayDto
import com.onislanguage.app.data.api.KanjiPredictionDto
import com.onislanguage.app.data.api.OnisApiClient
import com.onislanguage.app.data.api.TranscriptWordDto
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.data.api.FlashcardDto
import com.onislanguage.app.data.api.AnalyzedSentenceDto
import com.onislanguage.app.ui.viewmodel.AiViewModel
import com.onislanguage.app.ui.viewmodel.AuthViewModel
import com.onislanguage.app.ui.viewmodel.FlashcardViewModel
import com.onislanguage.app.ui.components.AuthPromptView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

private const val KANJI_EXPORT_SIZE = 128
private const val KANJI_EXPORT_PADDING = 12f
private const val KANJI_EXPORT_STROKE_WIDTH = 10f
private const val KANJI_CANVAS_STROKE_WIDTH = 16f

private fun mediaTitleFromName(name: String?): String {
    if (name.isNullOrBlank()) return "Untitled media"
    return name.substringBeforeLast(".")
}

@Composable
fun AiHubScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Column {
                    Text(stringResource(R.string.ai_tools), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text(stringResource(R.string.ai_hub_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(stringResource(R.string.ai_assistant), color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.ai_hub_desc),
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AiActionCard(Icons.Default.GraphicEq, stringResource(R.string.media_transcribe), stringResource(R.string.media_transcribe), {
                    onNavigate(Screen.AudioToText.route)
                }, Modifier.weight(1f))
                AiActionCard(Icons.Default.ImageSearch, "OCR", stringResource(R.string.ocr_desc), {
                    onNavigate(Screen.ImageAnalysis.route)
                }, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AiActionCard(Icons.Default.Create, stringResource(R.string.kanji_draw), stringResource(R.string.kanji_draw), {
                    onNavigate(Screen.Kanji.route)
                }, Modifier.weight(1f))
                AiActionCard(Icons.Default.Analytics, stringResource(R.string.analysis), stringResource(R.string.analysis), {
                    onNavigate(Screen.Analysis.route)
                }, Modifier.weight(1f))
            }
        }
    }
}


@Composable
fun AudioToTextScreenV2(
    onOpenResult: (Long) -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val history by viewModel.transcriptionHistory.collectAsState()
    val serverHistory by viewModel.serverTranscriptionHistory.collectAsState()
    val latestHistoryId by viewModel.latestTranscriptHistoryId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadLabel by viewModel.uploadLabel.collectAsState()
    val error by viewModel.error.collectAsState()
    var youtubeUrl by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("video") }
    val scrollState = rememberScrollState()

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val displayName = UriFileUtils.getDisplayName(context, it) ?: "audio.mp3"
            val file = UriFileUtils.uriToFile(context, it, "audio_${System.currentTimeMillis()}_${displayName}")
            file?.let { f ->
                viewModel.transcribeMedia(
                    mediaFile = f,
                    sourceType = "audio",
                    sourceUri = uri.toString(),
                    displayTitle = mediaTitleFromName(displayName),
                    uploadFileName = displayName
                )
            }
        }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val displayName = UriFileUtils.getDisplayName(context, it) ?: "video.mp4"
            val file = UriFileUtils.uriToFile(context, it, "video_${System.currentTimeMillis()}_${displayName}")
            file?.let { f ->
                viewModel.transcribeMedia(
                    mediaFile = f,
                    sourceType = "video",
                    sourceUri = uri.toString(),
                    displayTitle = mediaTitleFromName(displayName),
                    uploadFileName = displayName
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTranscriptionHistory()
    }

    LaunchedEffect(latestHistoryId) {
        latestHistoryId?.let {
            onOpenResult(it)
            viewModel.consumeLatestTranscriptHistoryId()
        }
    }
    val filteredHistory = remember(history, activeTab) {
        history.filter { item ->
            when (activeTab) {
                "audio" -> item.sourceType == "audio"
                "video" -> item.sourceType == "video"
                "youtube" -> item.sourceType == "youtube"
                "server" -> false
                else -> true
            }
        }
    }
    val filteredServerHistory = remember(serverHistory) {
        serverHistory.filter { item -> item.sourceType == "audio" || item.sourceType == "video" || item.sourceType == "youtube" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp)
    ) {
        HeaderSection("Import Media", "Đưa audio, video hoặc YouTube vào luồng transcript và học theo media.")
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("video", "audio", "youtube", "server").forEach { tab ->
                val isActive = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable { activeTab = tab }
                        .background(if (isActive) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            "youtube" -> "YouTube"
                            "server" -> "Server"
                            else -> tab.replaceFirstChar { it.uppercase() }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        if (activeTab != "server") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(32.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (activeTab) {
                            "audio" -> "Thêm audio vào workspace"
                            "youtube" -> "Nhập link YouTube"
                            else -> "Thêm video vào workspace"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (activeTab == "youtube") {
                        TextField(
                            value = youtubeUrl,
                            onValueChange = { youtubeUrl = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text("Paste YouTube link", color = MaterialTheme.colorScheme.outline) },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.outline) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (youtubeUrl.isNotBlank()) {
                                    viewModel.transcribeYouTube(youtubeUrl.trim())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Start analysis", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                when (activeTab) {
                                    "audio" -> audioLauncher.launch("audio/*")
                                    "video" -> videoLauncher.launch("video/*")
                                }
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (activeTab == "audio") "Browse audio" else "Browse video",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = when (activeTab) {
                            "audio" -> "MP3, WAV, M4A"
                            "youtube" -> "YouTube watch link"
                            else -> "MP4, MOV, MKV"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        if (isLoading) {
            Spacer(Modifier.height(20.dp))
            UploadProgressCard(
                label = uploadLabel,
                progress = uploadProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(if (activeTab == "server") "Media từ server" else "File đã dịch", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == "server" && filteredServerHistory.isEmpty()) {
            SectionCardV2 {
                Text("Chưa có media nào được dịch trên server.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (activeTab != "server" && filteredHistory.isEmpty()) {
            SectionCardV2 {
                Text("Chưa có media nào được dịch trên máy này.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (activeTab == "server") {
            filteredServerHistory.forEach { item ->
                MediaTranscriptHistoryCard(item = item) {
                    viewModel.importServerTranscriptHistory(item)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            filteredHistory.forEach { item ->
                MediaTranscriptHistoryCard(item = item) {
                    onOpenResult(item.id)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun AudioToTextResultScreenV2(
    historyId: Long?,
    onBack: () -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val selectedHistory by viewModel.selectedTranscriptHistory.collectAsState()
    val transcriptionResult by viewModel.transcriptionResult.collectAsState()
    val activeTranscriptLocalPath by viewModel.activeTranscriptLocalPath.collectAsState()
    val activeSegmentIndex by viewModel.activeTranscriptSegmentIndex.collectAsState()
    var mediaSeekTo by remember { mutableStateOf<(Int) -> Unit>({}) }
    var playbackPositionMs by remember { mutableIntStateOf(0) }
    val transcriptListState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(historyId) {
        historyId?.takeIf { it > 0 }?.let { viewModel.openTranscriptHistory(it) }
    }

    val history = selectedHistory
    val runtimeResult = transcriptionResult?.takeIf { it.full_text_ja == history?.fullTextJa }
    val displaySegments = runtimeResult?.segments ?: history?.segments ?: emptyList()
    val displayTitle = history?.title ?: runtimeResult?.media_title ?: "Đang tải..."
    val displayDuration = runtimeResult?.duration ?: history?.duration
    val localMediaUrl = (activeTranscriptLocalPath?.takeIf { runtimeResult != null } ?: history?.localMediaPath)?.let { path ->
        File(path).toUri().toString()
    }
    val mediaUrl = localMediaUrl ?: history?.mediaUrl?.let(OnisApiClient::resolveUrl) ?: history?.sourceUri
    val mediaKind = history?.mediaKind ?: history?.sourceType

    LaunchedEffect(activeSegmentIndex, displaySegments.size) {
        if (activeSegmentIndex >= 0 && activeSegmentIndex < displaySegments.size) {
            transcriptListState.animateScrollToItem(activeSegmentIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Column {
                Text("Kết quả transcript", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(
                    displayTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        displayDuration?.let {
            Text(
                "Thời lượng: ${formatTranscriptTime(it)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        mediaUrl?.let { resolvedUrl ->
            if (mediaKind == "audio") {
                AudioTranscriptPlayer(
                    mediaUrl = resolvedUrl,
                    onProgress = { positionMs ->
                        playbackPositionMs = positionMs
                        viewModel.updateActiveTranscriptSegment(positionMs)
                    },
                    onReady = { mediaSeekTo = it }
                )
            } else {
                VideoTranscriptPlayer(
                    mediaUrl = resolvedUrl,
                    onProgress = { positionMs ->
                        playbackPositionMs = positionMs
                        viewModel.updateActiveTranscriptSegment(positionMs)
                    },
                    onReady = { mediaSeekTo = it }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("Transcript", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = transcriptListState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(displaySegments) { index, segment ->
                TranscriptCardV2(
                    TranscriptSegmentV2(
                        segment.start,
                        segment.end,
                        segment.text_ja,
                        segment.text_vi ?: "",
                        segment.text_display,
                        segment.words
                    ),
                    false,
                    currentPlaybackMs = playbackPositionMs,
                    onClick = {
                        val targetMs = (segment.start * 1000).toInt()
                        mediaSeekTo(targetMs)
                        playbackPositionMs = targetMs
                        viewModel.updateActiveTranscriptSegment(targetMs)
                    }
                )
            }
            item { Spacer(Modifier.height(64.dp)) }
        }
    }
}

@Composable
private fun MediaTranscriptHistoryCard(
    item: com.onislanguage.app.data.model.MediaTranscriptHistoryItem,
    onClick: () -> Unit
) {
    SectionCardV2 {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        if (item.mediaKind == "audio") Icons.Default.GraphicEq else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${item.sourceType.replaceFirstChar { it.uppercase() }} • ${formatTranscriptTime(item.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ImageAnalysisScreenV2(
    onOpenResult: (Long) -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val history by viewModel.ocrHistory.collectAsState()
    val latestHistoryId by viewModel.latestOcrHistoryId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadLabel by viewModel.uploadLabel.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var localNotice by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val displayName = UriFileUtils.getDisplayName(context, it) ?: "image.jpg"
            val sourceUri = it.toString()
            val file = UriFileUtils.uriToFile(context, it, "ocr_${System.currentTimeMillis()}_$displayName")
            selectedImageUri = it
            localNotice = null
            file?.let { f -> viewModel.ocrImage(f, sourceUri, mediaTitleFromName(displayName)) }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            localNotice = "Không chụp được ảnh."
            pendingCameraUri = null
            return@rememberLauncherForActivityResult
        }
        pendingCameraUri?.let { uri ->
            selectedImageUri = uri
            val file = UriFileUtils.uriToFile(context, uri, "ocr_camera_${System.currentTimeMillis()}.jpg")
            file?.let { f ->
                localNotice = null
                viewModel.ocrImage(f, uri.toString(), "Camera capture")
            }
        }
        pendingCameraUri = null
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val uri = pendingCameraUri
        if (granted && uri != null) {
            cameraLauncher.launch(uri)
        } else if (!granted) {
            localNotice = "Cần cấp quyền camera để chụp ảnh."
            pendingCameraUri = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadOcrHistory()
    }

    LaunchedEffect(latestHistoryId) {
        latestHistoryId?.let {
            onOpenResult(it)
            viewModel.consumeLatestOcrHistoryId()
        }
    }

    ScreenScaffoldV2("OCR Ảnh", stringResource(R.string.ocr_desc)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.ImageSearch, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detect ảnh", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Chọn ảnh từ máy hoặc chụp trực tiếp, OCR sẽ trả text detect được.",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                UploadCardV2(
                    icon = Icons.Default.ImageSearch,
                    title = "Chọn ảnh",
                    subtitle = "Từ thư viện",
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    height = 150.dp
                )
                UploadCardV2(
                    icon = Icons.Default.PhotoCamera,
                    title = "Chụp ảnh",
                    subtitle = "Mở camera",
                    onClick = {
                        val uri = UriFileUtils.createTempImageUri(context)
                        pendingCameraUri = uri
                        val permissionGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (permissionGranted) {
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    height = 150.dp
                )
            }

            selectedImageUri?.let { previewUri ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Ảnh đã chọn", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        AsyncImage(
                            model = previewUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 260.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (isLoading) {
                UploadProgressCard(
                    label = uploadLabel,
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            localNotice?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Text("Lịch sử ảnh", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))

            if (history.isEmpty()) {
                SectionCardV2 {
                    Text("Chưa có ảnh nào được detect trên máy này.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                history.forEach { item ->
                    OcrHistoryCard(item = item, onClick = { onOpenResult(item.id) })
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun UploadProgressCard(
    label: String?,
    progress: Int?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        SectionCardV2 {
            Text(
                label ?: "Đang xử lý",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { (progress.coerceIn(0, 100) / 100f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$progress%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun OcrHistoryCard(
    item: com.onislanguage.app.data.model.OcrImageHistoryItem,
    onClick: () -> Unit
) {
    SectionCardV2 {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(
                    item.fullText.ifBlank { "Chưa detect được text." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ImageAnalysisResultScreenV2(
    historyId: Long?,
    onBack: () -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val selectedHistory by viewModel.selectedOcrHistory.collectAsState()
    val ocrResult by viewModel.ocrResult.collectAsState()
    val history = selectedHistory
    val runtimeResult = ocrResult?.takeIf { it.full_text == history?.fullText }

    LaunchedEffect(historyId) {
        historyId?.let { viewModel.openOcrHistory(it) }
    }

    val imageUrl = history?.imageUrl?.let(OnisApiClient::resolveUrl) ?: history?.sourceUri

    ScreenScaffoldV2("Kết quả OCR", history?.title ?: "Đang tải...") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Column {
                Text("Kết quả detect ảnh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(history?.title ?: "Đang tải...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        imageUrl?.let { resolvedUrl ->
            AsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 280.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
        }

        SectionCardV2 {
            Text("Text detect được", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            runtimeResult?.sentences?.takeIf { it.isNotEmpty() }?.let { sentences ->
                SentenceDisplayList(sentences.map { it.text_display })
            } ?: runtimeResult?.text_display?.let { display ->
                JapaneseTextDisplayCard(display = display)
            } ?: history?.textDisplay?.let { display ->
                JapaneseTextDisplayCard(display = display)
            } ?: Text(history?.fullText?.ifBlank { "Không detect được text." } ?: "Đang tải...")
        }

        if (runtimeResult?.text_display == null && history?.textDisplay == null) history?.translatedTextVi?.let {
            Spacer(Modifier.height(12.dp))
            SectionCardV2 {
                Text("Dịch nghĩa", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(it)
            }
        }
    }
}

@Composable
fun KanjiScreenV2(
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val kanjiResult by viewModel.kanjiResult.collectAsState()
    val points by viewModel.kanjiStrokes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedKanji by viewModel.selectedKanji.collectAsState()

    ScreenScaffoldV2(stringResource(R.string.kanji_draw), stringResource(R.string.ai_hub_desc)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { viewModel.startKanjiStroke(it) },
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.appendKanjiStrokePoint(change.position)
                            }
                        )
                    }
            ) {
                val grid = Color(0xFFF0F0F0)
                drawLine(grid, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2f)
                drawLine(grid, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2f)
                
                points.forEach { stroke ->
                    if (stroke.size == 1) {
                        drawCircle(
                            color = Color.Black,
                            radius = KANJI_CANVAS_STROKE_WIDTH / 2f,
                            center = stroke.first()
                        )
                    } else {
                        val path = Path()
                        stroke.forEachIndexed { index, offset ->
                            if (index == 0) path.moveTo(offset.x, offset.y)
                            else path.lineTo(offset.x, offset.y)
                        }
                        drawPath(
                            path,
                            Color.Black,
                            style = Stroke(
                                width = KANJI_CANVAS_STROKE_WIDTH,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { viewModel.clearKanjiDrawing() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Clear, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.delete))
            }
            Button(
                onClick = {
                    if (points.isEmpty()) return@Button
                    val bitmap = Bitmap.createBitmap(KANJI_EXPORT_SIZE, KANJI_EXPORT_SIZE, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    val paint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        strokeWidth = KANJI_EXPORT_STROKE_WIDTH
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        isAntiAlias = true
                    }
                    
                    // Export the normalized drawing directly at the model input size.
                    if (points.isNotEmpty()) {
                        val allPoints = points.flatten()
                        val minX = allPoints.minOf { it.x }
                        val maxX = allPoints.maxOf { it.x }
                        val minY = allPoints.minOf { it.y }
                        val maxY = allPoints.maxOf { it.y }
                        
                        val drawWidth = maxX - minX
                        val drawHeight = maxY - minY
                        val targetSize = KANJI_EXPORT_SIZE.toFloat()
                        val scale = (targetSize - (KANJI_EXPORT_PADDING * 2f)) / maxOf(drawWidth, drawHeight, 1f)
                        val offsetX = (targetSize - drawWidth * scale) / 2f
                        val offsetY = (targetSize - drawHeight * scale) / 2f

                        points.forEach { stroke ->
                            if (stroke.size == 1) {
                                val point = stroke.first()
                                val px = (point.x - minX) * scale + offsetX
                                val py = (point.y - minY) * scale + offsetY
                                canvas.drawCircle(px, py, KANJI_EXPORT_STROKE_WIDTH / 2f, paint)
                            } else {
                                val androidPath = android.graphics.Path()
                                stroke.forEachIndexed { index, offset ->
                                    val px = (offset.x - minX) * scale + offsetX
                                    val py = (offset.y - minY) * scale + offsetY
                                    if (index == 0) androidPath.moveTo(px, py)
                                    else androidPath.lineTo(px, py)
                                }
                                canvas.drawPath(androidPath, paint)
                            }
                        }
                    }

                    val file = File(context.cacheDir, "kanji_draw.png")
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    viewModel.recognizeKanji(file)
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.verify))
                }
            }
        }
        
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        kanjiResult?.let { res ->
            Spacer(Modifier.height(18.dp))
            Text("Kết quả nhận diện", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(res.top5) { candidate ->
                    KanjiCandidateCard(
                        kanji = candidate.kanji,
                        confidence = candidate.confidence,
                        meaning = candidate.meaning_vi ?: "",
                        selected = selectedKanji == candidate.kanji,
                        onClick = { viewModel.selectKanji(candidate.kanji) }
                    )
                }
            }

            selectedKanji?.let { sel ->
                val match = res.top5.find { it.kanji == sel }
                match?.let {
                    Spacer(Modifier.height(20.dp))
                    KanjiDetailsSection(candidate = it)
                }
            }
        }
    }
}

@Composable
fun LoginScreenV2(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    var email by remember { mutableStateOf("demo@onis.app") }
    var password by remember { mutableStateOf("123456") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("User Demo") }
    var otp by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    
    var isRegisterMode by remember { mutableStateOf(false) }
    var isOtpStage by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val serverError by viewModel.error.collectAsState()
    val error = localError ?: serverError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFEAF4FF), Color(0xFFFFF2E6))))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Text(
                    if (isOtpStage) stringResource(R.string.otp_hint)
                    else if (isRegisterMode) stringResource(R.string.register_hint)
                    else stringResource(R.string.login_sync_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))

                if (isOtpStage) {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.otp_code)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                } else {
                    AnimatedVisibility(visible = isRegisterMode) {
                        Column {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.display_name)) },
                                singleLine = true
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    AnimatedVisibility(visible = isRegisterMode) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.confirm_password)) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }
                    }
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        localError = null
                        if (isOtpStage) {
                            viewModel.verifyOtp(email, otp, onLoginSuccess)
                        } else if (isRegisterMode) {
                            if (password != confirmPassword) {
                                localError = "Mật khẩu xác nhận không khớp"
                                return@Button
                            }
                            viewModel.register(email, password, confirmPassword, displayName) {
                                isOtpStage = true
                            }
                        } else {
                            viewModel.login(email, password, onLoginSuccess)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        val label = if (isOtpStage) stringResource(R.string.verify) else if (isRegisterMode) stringResource(R.string.send_otp) else stringResource(R.string.login)
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (!isOtpStage) {
                    TextButton(
                        onClick = { 
                            isRegisterMode = !isRegisterMode 
                            localError = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRegisterMode) stringResource(R.string.have_account) else stringResource(R.string.no_account))
                    }
                } else {
                    TextButton(
                        onClick = { isOtpStage = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.back))
                    }
                }
            }
        }
    }
}

@Composable
fun JapaneseHomeScreenV2(
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    var input by remember { mutableStateOf("日本語を勉強しています。") }
    var showAllTranslationHistory by remember { mutableStateOf(false) }
    var selectedTranslationHistory by remember { mutableStateOf<com.onislanguage.app.data.model.TranslationHistoryItem?>(null) }
    val translationResult by viewModel.homeTranslationResult.collectAsState()
    val translationHistory by viewModel.translationHistory.collectAsState()
    val isLanguageLoading by viewModel.isLanguageLoading.collectAsState()
    val languageError by viewModel.languageError.collectAsState()
    val translationDisplay = translationResult?.text_display
    val previewTranslatedText = selectedTranslationHistory?.translatedText
        ?: translationResult?.translated_text?.trim().orEmpty()

    LaunchedEffect(Unit) {
        viewModel.loadTranslationHistory()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.onis_logo),
                            contentDescription = "OnisLanguage",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("OnisLanguage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                    }
                    FilledTonalIconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            }
        }

        item {
            SectionCardV2 {
                Text(
                    "Dịch Nhật - Việt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập tiếng Nhật hoặc tiếng Việt...") },
                    minLines = 4,
                    shape = RoundedCornerShape(18.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val source = input.trim()
                        selectedTranslationHistory = null
                        viewModel.translateForHome(source, viewModel.detectHomeSourceLanguage(source))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = input.trim().isNotBlank() && !isLanguageLoading
                ) {
                    Text("Dịch")
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val isViToJa = selectedTranslationHistory?.direction == "vi_ja" ||
                            (selectedTranslationHistory == null && viewModel.detectHomeSourceLanguage(input) == "vi")
                        Text(
                            if (selectedTranslationHistory != null) "Bản dịch đã lưu" else if (isViToJa) "Kết quả tiếng Nhật" else "Kết quả tiếng Việt",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        when {
                            isLanguageLoading -> {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            }
                            translationDisplay != null && selectedTranslationHistory == null -> {
                                JapaneseTextDisplayCard(display = translationDisplay)
                            }
                            previewTranslatedText.isNotBlank() -> {
                                Text(
                                    previewTranslatedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            else -> {
                                Text(
                                    "Kết quả sẽ hiện ở đây.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                languageError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            SectionCardV2 {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lịch sử dịch", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (translationHistory.size > 3) {
                        TextButton(
                            onClick = { showAllTranslationHistory = !showAllTranslationHistory },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (showAllTranslationHistory) "Thu gọn" else "Xem thêm")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (translationHistory.isEmpty()) {
                    Text("Chưa có bản dịch nào được lưu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val visibleItems = if (showAllTranslationHistory) translationHistory else translationHistory.take(3)
                    visibleItems.forEach { item ->
                        TranslationHistoryRow(
                            item = item,
                            selected = selectedTranslationHistory?.id == item.id,
                            onClick = {
                                input = item.sourceText
                                selectedTranslationHistory = item
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            SectionCardV2 {
                Text(stringResource(R.string.recommended_features), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToolTileV2(Icons.Default.Create, "Vẽ kanji", "Nhận diện chữ viết tay", { onNavigate(Screen.Kanji.route) })
                    ToolTileV2(Icons.Default.GraphicEq, "Audio", "Chuyển lời nói thành văn bản", { onNavigate(Screen.AudioToText.route) })
                }
            }
        }
    }
}

@Composable
private fun TranslationHistoryRow(
    item: com.onislanguage.app.data.model.TranslationHistoryItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    }
                ) {
                    Text(
                        text = if (item.direction == "ja_vi") "Nhật -> Việt" else "Việt -> Nhật",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatTranslationHistoryTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TranslationHistoryTextBlock(
                label = "Văn bản gốc",
                text = item.sourceText
            )
            TranslationHistoryTextBlock(
                label = "Bản dịch",
                text = item.translatedText,
                emphasize = selected
            )
        }
    }
}

@Composable
private fun TranslationHistoryTextBlock(
    label: String,
    text: String,
    emphasize: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun formatTranslationHistoryTime(createdAt: Long): String {
    return android.text.format.DateFormat.format("dd/MM HH:mm", createdAt).toString()
}

@Composable
fun FlashcardScreenV2(
    onNavigate: (String) -> Unit,
    viewModel: FlashcardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideFlashcardViewModel() as T
        }
    }),
    authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: My Decks, 1: Server
    var studyingDeckId by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val localDecks by viewModel.localDecks.collectAsState()
    val remoteDecks by viewModel.remoteDecks.collectAsState()
    val currentCards by viewModel.currentCards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val importNotice by viewModel.importNotice.collectAsState()

    val fileImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val displayName = UriFileUtils.getDisplayName(context, uri)?.substringBeforeLast(".")?.ifBlank { "Flashcard AI" } ?: "Flashcard AI"
        val content = UriFileUtils.extractTextFromUri(context, uri)
        viewModel.generateDeckFromText(displayName, content)
    }

    LaunchedEffect(Unit) {
        viewModel.loadLocalDecks()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && authState != null) {
            viewModel.loadRemoteDecks()
        }
    }

    if (studyingDeckId != null) {
        FlashcardStudyView(
            cards = currentCards,
            onBack = { studyingDeckId = null },
            onAddCard = { front, back, example -> viewModel.addCard(studyingDeckId!!, front, back, example) }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            Text(stringResource(R.string.flashcards), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            
            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(stringResource(R.string.my_decks), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(stringResource(R.string.server_decks), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            importNotice?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedTab == 1 && authState == null) {
                AuthPromptView(onLoginClick = { onNavigate(Screen.Login.route) })
            } else if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (selectedTab == 0) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Button(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.create_new_deck), fontWeight = FontWeight.Bold)
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { fileImportLauncher.launch("*/*") },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Tạo bằng AI từ file", fontWeight = FontWeight.Bold)
                            }
                        }
                        items(localDecks) { deck ->
                            DeckCardV2(
                                title = deck.title,
                                cardCount = deck.cards.size,
                                onLearn = { 
                                    viewModel.selectDeck(deck.deck_id)
                                    studyingDeckId = deck.deck_id 
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(remoteDecks) { deck ->
                            ServerDeckCardV2(
                                title = deck.title,
                                onDownload = { viewModel.downloadDeck(deck.deck_id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var deckName by remember { mutableStateOf("") }
        var deckDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.create_new_deck)) },
            confirmButton = {
                Button(onClick = {
                    if (deckName.isNotBlank()) {
                        viewModel.createDeck(deckName, deckDesc)
                        showCreateDialog = false
                    }
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = deckName, onValueChange = { deckName = it }, label = { Text(stringResource(R.string.deck_name)) })
                    OutlinedTextField(value = deckDesc, onValueChange = { deckDesc = it }, label = { Text(stringResource(R.string.description)) })
                }
            }
        )
    }
}

@Composable
fun FlashcardStudyView(
    cards: List<FlashcardDto>, 
    onBack: () -> Unit,
    onAddCard: (String, String, String) -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var showAddCard by remember { mutableStateOf(false) }
    
    if (cards.isEmpty() && !showAddCard) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.no_cards_in_deck))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showAddCard = true }) { Text("Thêm thẻ đầu tiên") }
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            }
        }
    } else if (showAddCard) {
        var front by remember { mutableStateOf("") }
        var back by remember { mutableStateOf("") }
        var example by remember { mutableStateOf("") }
        
        ScreenScaffoldV2("Thêm thẻ mới", "") {
            OutlinedTextField(front, { front = it }, Modifier.fillMaxWidth(), label = { Text("Mặt trước") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(back, { back = it }, Modifier.fillMaxWidth(), label = { Text("Mặt sau") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(example, { example = it }, Modifier.fillMaxWidth(), label = { Text("Câu ví dụ") })
            Spacer(Modifier.height(24.dp))
            Button(onClick = { 
                if (front.isNotBlank()) {
                    onAddCard(front, back, example)
                    showAddCard = false
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(stringResource(R.string.save))
            }
            TextButton(onClick = { showAddCard = false }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel))
            }
        }
    } else {
        val card = cards[index.floorMod(cards.size)]
        val rotation by animateFloatAsState(
            targetValue = if (flipped) 180f else 0f,
            label = "flashcardRotation"
        )
        val showBack = rotation > 90f

        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                Text(stringResource(R.string.studying), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddCard = true }) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(40.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { flipped = !flipped },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .graphicsLayer {
                            if (showBack) {
                                rotationY = 180f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (showBack) card.back else card.front, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(12.dp))
                        Text(if (showBack) (card.example_sentence ?: "") else stringResource(R.string.tap_to_see_meaning), color = Color.White.copy(alpha = 0.75f))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = { index--; flipped = false }, modifier = Modifier.weight(1f).height(56.dp)) { Text(stringResource(R.string.previous)) }
                Button(onClick = { index++; flipped = false }, modifier = Modifier.weight(1f).height(56.dp)) { Text(stringResource(R.string.next)) }
            }
        }
    }
}

@Composable
private fun ScreenScaffoldV2(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(24.dp))
        } else {
            Spacer(Modifier.height(12.dp))
        }
        content()
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SectionCardV2(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun RowScope.ToolTileV2(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(132.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RowScope.CompactShortcutChip(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DeckCardV2(title: String, cardCount: Int, onLearn: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Style, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("$cardCount ${stringResource(R.string.flashcards)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onLearn,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(stringResource(R.string.learn_now)) }
        }
    }
}

@Composable
private fun ServerDeckCardV2(title: String, onDownload: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Chia sẻ bởi cộng đồng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                modifier = Modifier.clickable(onClick = onDownload),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun KanjiCandidateCard(kanji: String, confidence: Float, meaning: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(kanji, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("${(confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            Text(
                text = if (meaning.isBlank()) "Chưa có nghĩa" else meaning,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UploadCardV2(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 150.dp
) {
    Surface(
        modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun KanjiDetailsSection(candidate: KanjiPredictionDto) {
    val details = candidate.details ?: return

    Spacer(Modifier.height(16.dp))
    SectionCardV2 {
        Text("Phân tích chi tiết", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        KanjiDetailLine("Nghĩa Hán", details.am_han ?: "Chưa có dữ liệu")
        KanjiDetailLine("Cách đọc", candidate.reading ?: "Chưa có dữ liệu")
        KanjiDetailLine("Nghĩa tiếng Việt", details.meaning_vi ?: candidate.meaning_vi ?: "Chưa có dữ liệu")
        KanjiDetailLine("Số nét", details.stroke_count?.toString() ?: "Chưa có dữ liệu")
        if (details.on_readings.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            KanjiDetailLine("Âm On", details.on_readings.joinToString())
        }
        if (details.kun_readings.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            KanjiDetailLine("Âm Kun", details.kun_readings.joinToString())
        }
        if (!details.meaning_en.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            KanjiDetailLine("Nghĩa tiếng Anh", details.meaning_en)
        }
        if (details.examples.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            KanjiDetailLine("Ví dụ", details.examples.joinToString())
        }
        if (!details.explanation.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("Giải thích", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                details.explanation,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    details.svg_url?.let { svgUrl ->
        Spacer(Modifier.height(16.dp))
        SectionCardV2 {
            Text("Thứ tự nét", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            AnimatedKanjiSvg(
                svgUrl = OnisApiClient.resolveUrl(svgUrl),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }

    if (details.image_urls.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        SectionCardV2 {
            Text("Ảnh mô tả", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(details.image_urls) { imageUrl ->
                    AsyncImage(
                        model = OnisApiClient.resolveUrl(imageUrl),
                        contentDescription = "Kanji mnemonic image for ${candidate.kanji}",
                        modifier = Modifier
                            .width(220.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun KanjiDetailLine(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("$label: ")
            pop()
            pushStyle(SpanStyle(fontWeight = FontWeight.Normal))
            append(value)
            pop()
        },
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun AnimatedKanjiSvg(
    svgUrl: String?,
    modifier: Modifier = Modifier
) {
    if (svgUrl.isNullOrBlank()) {
        Box(
            modifier = modifier.background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("SVG not available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val context = LocalContext.current
    val svgImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    SubcomposeAsyncImage(
        imageLoader = svgImageLoader,
        model = ImageRequest.Builder(context)
            .data(svgUrl)
            .crossfade(false)
            .build(),
        contentDescription = "Stroke order SVG",
        modifier = modifier.background(Color.White),
        contentScale = ContentScale.Fit,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("Không hiển thị được thứ tự nét", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

private fun translatePreviewV2(text: String, mode: String): String {
    val jaVi = mapOf("日本語" to "tiếng Nhật", "勉強" to "học tập", "先生" to "giáo viên", "学生" to "học sinh", "富士山" to "núi Phú Sĩ", "日本" to "Nhật Bản", "山" to "núi", "木" to "cây/mộc", "水" to "nước")
    val viJa = mapOf("tiếng nhật" to "日本語", "học tập" to "勉強", "giáo viên" to "先生", "học sinh" to "学生", "nhật bản" to "日本", "núi" to "山", "nước" to "水")
    
    if (mode == "ja_vi") {
        var output = text
        jaVi.forEach { (ja, vi) -> output = output.replace(ja, vi) }
        return output.replace("。", ".")
    } else {
        var output = text.lowercase()
        viJa.forEach { (vi, ja) -> output = output.replace(vi, ja) }
        return output
    }
}

private fun analyzePreviewV2(text: String): String {
    val found = listOf("日本語", "勉強", "先生", "学生", "富士山", "日本", "山", "木", "水").filter { text.contains(it) }
    return "Từ vựng: ${found.ifEmpty { listOf("chưa phát hiện") }.joinToString()}."
}

private fun Int.floorMod(size: Int): Int = if (size > 0) ((this % size) + size) % size else 0

@Composable
private fun AiActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class TranscriptSegmentV2(
    val start: Float,
    val end: Float,
    val text_ja: String,
    val text_vi: String,
    val textDisplay: JapaneseTextDisplayDto? = null,
    val words: List<TranscriptWordDto> = emptyList()
)

@Composable
fun TranscriptCardV2(
    segment: TranscriptSegmentV2,
    isSelected: Boolean,
    currentPlaybackMs: Int,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${segment.start}s - ${segment.end}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (segment.words.isNotEmpty()) {
                TranscriptWordFlow(
                    words = segment.words,
                    currentPlaybackMs = currentPlaybackMs
                )
                if (segment.text_vi.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = segment.text_vi, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (segment.textDisplay != null) {
                JapaneseTextDisplayCard(display = segment.textDisplay)
            } else {
                Text(text = segment.text_ja, fontWeight = FontWeight.Bold)
                if (segment.text_vi.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = segment.text_vi, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun JapaneseTextDisplayCard(display: JapaneseTextDisplayDto) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        JapaneseFuriganaText(tokens = display.tokens, fallbackText = display.text)
        display.translation_vi?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SentenceDisplayList(
    sentences: List<JapaneseTextDisplayDto>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sentences.forEachIndexed { index, display ->
            JapaneseTextDisplayCard(display = display)
            if (index != sentences.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JapaneseFuriganaText(
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
            FuriganaTokenView(token = token)
        }
    }
}

@Composable
private fun FuriganaTokenView(
    token: FuriganaTokenDto,
    isHighlighted: Boolean = false
) {
    val posTint = partOfSpeechTint(token.part_of_speech)
    val surfaceColor = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val readingColor = if (isHighlighted) MaterialTheme.colorScheme.primary else posTint
    val readingSlotHeight = 12.dp
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                else Color.Transparent
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
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
                    color = readingColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = token.surface,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = surfaceColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TranscriptWordFlow(
    words: List<TranscriptWordDto>,
    currentPlaybackMs: Int
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        words.forEach { word ->
            val isHighlighted = currentPlaybackMs in (word.start * 1000).toInt()..(word.end * 1000).toInt()
            val display = word.text_display
            if (display != null && display.tokens.isNotEmpty()) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        display.tokens.forEach { token ->
                            FuriganaTokenView(token = token, isHighlighted = isHighlighted)
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f) else Color.Transparent
                ) {
                    Text(
                        text = word.text_ja,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun partOfSpeechTint(partOfSpeech: String?): Color {
    return when (partOfSpeech) {
        "名詞" -> MaterialTheme.colorScheme.primary
        "動詞" -> Color(0xFF1B8A5A)
        "形容詞" -> Color(0xFFD17B0F)
        "副詞" -> Color(0xFF7A56C5)
        "助詞" -> Color(0xFF5C6B73)
        else -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun VideoTranscriptPlayer(
    mediaUrl: String?,
    onProgress: (Int) -> Unit,
    onReady: ((Int) -> Unit) -> Unit
) {
    val url = mediaUrl ?: return
    var videoView by remember(url) { mutableStateOf<VideoView?>(null) }
    var isPrepared by remember(url) { mutableStateOf(false) }
    var isPlaying by remember(url) { mutableStateOf(false) }
    var durationMs by remember(url) { mutableIntStateOf(0) }
    var positionMs by remember(url) { mutableIntStateOf(0) }
    var sliderPosition by remember(url) { mutableFloatStateOf(0f) }
    var isDragging by remember(url) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp)),
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse(url))
                    setOnPreparedListener { player ->
                        videoView = this
                        isPrepared = true
                        durationMs = player.duration.coerceAtLeast(0)
                        onReady { targetMs ->
                            seekTo(targetMs)
                            positionMs = targetMs
                            sliderPosition = targetMs.toFloat()
                            onProgress(targetMs)
                        }
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        positionMs = durationMs
                        sliderPosition = durationMs.toFloat()
                        onProgress(durationMs)
                    }
                }
            },
            update = {
                if (it.tag != url) {
                    it.tag = url
                    it.setVideoURI(Uri.parse(url))
                    it.start()
                }
            }
        )

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = {
                    val player = videoView ?: return@FilledIconButton
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else if (isPrepared) {
                        player.start()
                        isPlaying = true
                    }
                }
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "${formatTranscriptTime(positionMs / 1000f)} / ${formatTranscriptTime(durationMs / 1000f)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = sliderPosition,
            onValueChange = {
                isDragging = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                val targetMs = sliderPosition.toInt()
                videoView?.seekTo(targetMs)
                positionMs = targetMs
                onProgress(targetMs)
                isDragging = false
            },
            valueRange = 0f..durationMs.coerceAtLeast(1).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    LaunchedEffect(videoView, isPrepared, url) {
        while (true) {
            val currentPlayer = videoView
            if (currentPlayer != null && isPrepared) {
                val currentPosition = currentPlayer.currentPosition
                positionMs = currentPosition
                if (!isDragging) {
                    sliderPosition = currentPosition.toFloat()
                }
                isPlaying = currentPlayer.isPlaying
                onProgress(currentPosition)
            }
            kotlinx.coroutines.delay(250)
        }
    }
}

@Composable
private fun AudioTranscriptPlayer(
    mediaUrl: String?,
    onProgress: (Int) -> Unit,
    onReady: ((Int) -> Unit) -> Unit
) {
    val url = mediaUrl ?: return
    var mediaPlayer by remember(url) { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember(url) { mutableStateOf(false) }
    var isPlaying by remember(url) { mutableStateOf(false) }
    var durationMs by remember(url) { mutableIntStateOf(0) }
    var positionMs by remember(url) { mutableIntStateOf(0) }
    var sliderPosition by remember(url) { mutableFloatStateOf(0f) }
    var isDragging by remember(url) { mutableStateOf(false) }

    DisposableEffect(url) {
        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener { preparedPlayer ->
                mediaPlayer = preparedPlayer
                isPrepared = true
                durationMs = preparedPlayer.duration.coerceAtLeast(0)
                onReady { targetMs ->
                    preparedPlayer.seekTo(targetMs)
                    positionMs = targetMs
                    sliderPosition = targetMs.toFloat()
                    onProgress(targetMs)
                }
                preparedPlayer.start()
                isPlaying = true
            }
            setOnCompletionListener {
                isPlaying = false
                positionMs = durationMs
                sliderPosition = durationMs.toFloat()
                onProgress(durationMs)
            }
            prepareAsync()
        }

        onDispose {
            runCatching {
                player.stop()
            }
            player.release()
        }
    }

    LaunchedEffect(mediaPlayer, isPrepared) {
        while (true) {
            val currentPlayer = mediaPlayer
            if (currentPlayer != null && isPrepared) {
                val currentPosition = currentPlayer.currentPosition
                positionMs = currentPosition
                if (!isDragging) {
                    sliderPosition = currentPosition.toFloat()
                }
                isPlaying = currentPlayer.isPlaying
                onProgress(currentPosition)
            }
            kotlinx.coroutines.delay(250)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = {
                    val player = mediaPlayer ?: return@FilledIconButton
                    if (!isPrepared) return@FilledIconButton
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                    }
                },
                enabled = isPrepared
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Audio preview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatTranscriptTime(positionMs / 1000f)} / ${formatTranscriptTime(durationMs / 1000f)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Slider(
            value = sliderPosition.coerceAtMost(durationMs.toFloat().coerceAtLeast(0f)),
            onValueChange = {
                isDragging = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                val targetMs = sliderPosition.toInt()
                mediaPlayer?.seekTo(targetMs)
                positionMs = targetMs
                onProgress(targetMs)
                isDragging = false
            },
            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
            enabled = isPrepared
        )
    }
}

private fun formatTranscriptTime(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remainder = totalSeconds % 60
    return "%d:%02d".format(minutes, remainder)
}
