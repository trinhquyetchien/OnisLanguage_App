package com.onislanguage.app.ui.screens
import com.onislanguage.app.utils.UriFileUtils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.onislanguage.app.R
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.data.api.FlashcardDto
import com.onislanguage.app.ui.viewmodel.AiViewModel
import com.onislanguage.app.ui.viewmodel.AuthViewModel
import com.onislanguage.app.ui.viewmodel.FlashcardViewModel
import com.onislanguage.app.ui.components.AuthPromptView
import java.io.File
import java.io.FileOutputStream

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
                    onNavigate(Screen.Dashboard.route)
                }, Modifier.weight(1f))
            }
        }
    }
}


@Composable
fun AudioToTextScreenV2(
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val result by viewModel.transcriptionResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = UriFileUtils.uriToFile(context, it, "upload_audio.mp3")
            file?.let { f -> viewModel.transcribeMedia(f) }
        }
    }

    ScreenScaffoldV2(stringResource(R.string.media_transcribe), stringResource(R.string.ai_hub_desc)) {
        UploadCardV2(Icons.Default.GraphicEq, stringResource(R.string.media_transcribe), "Hỗ trợ MP3, WAV, MP4 (max 25MB)") {
            launcher.launch("audio/*")
        }

        if (isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        result?.let { res ->
            Spacer(Modifier.height(24.dp))
            Text("Kết quả nhận diện", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            res.segments.forEach { segment ->
                TranscriptCardV2(
                    TranscriptSegmentV2(
                        segment.start, 
                        segment.end, 
                        segment.text_ja, 
                        segment.text_vi ?: ""
                    ), 
                    false
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ImageAnalysisScreenV2(
    viewModel: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAiViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val result by viewModel.ocrResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = UriFileUtils.uriToFile(context, it, "upload_image.jpg")
            file?.let { f -> viewModel.ocrImage(f) }
        }
    }

    ScreenScaffoldV2("OCR Ảnh", stringResource(R.string.ocr_desc)) {
        UploadCardV2(Icons.Default.ImageSearch, "Chọn ảnh", "Hoặc chụp ảnh trực tiếp") {
            launcher.launch("image/*")
        }

        if (isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        result?.let { res ->
            Spacer(Modifier.height(24.dp))
            SectionCardV2 {
                Text(res.full_text, fontWeight = FontWeight.Bold)
                res.translated_text_vi?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
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
    val points = remember { mutableStateListOf<List<Offset>>() }
    val context = LocalContext.current
    
    val kanjiResult by viewModel.kanjiResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedKanji by remember { mutableStateOf<String?>(null) }

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
                            onDragStart = { points.add(listOf(it)) },
                            onDrag = { change, _ ->
                                val lastList = points.last().toMutableList()
                                lastList.add(change.position)
                                points[points.size - 1] = lastList
                            }
                        )
                    }
            ) {
                val grid = Color(0xFFF0F0F0)
                drawLine(grid, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2f)
                drawLine(grid, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2f)
                
                points.forEach { stroke ->
                    val path = Path()
                    stroke.forEachIndexed { index, offset ->
                        if (index == 0) path.moveTo(offset.x, offset.y)
                        else path.lineTo(offset.x, offset.y)
                    }
                    drawPath(path, Color.Black, style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
        
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { points.clear() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Clear, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.delete))
            }
            Button(
                onClick = {
                    if (points.isEmpty()) return@Button
                    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    val paint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        strokeWidth = 24f
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        isAntiAlias = true
                    }
                    
                    points.forEach { stroke ->
                        val androidPath = android.graphics.Path()
                        stroke.forEachIndexed { index, offset ->
                            // Scale coordinates from Canvas size to 512x512
                            // (Needs actual canvas size, simplified here)
                            if (index == 0) androidPath.moveTo(offset.x, offset.y)
                            else androidPath.lineTo(offset.x, offset.y)
                        }
                        canvas.drawPath(androidPath, paint)
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
                        onClick = { selectedKanji = candidate.kanji }
                    )
                }
            }

            selectedKanji?.let { sel ->
                val match = res.top5.find { it.kanji == sel }
                match?.let {
                    Spacer(Modifier.height(20.dp))
                    ResultBlockV2(
                        "Chi tiết: $sel",
                        "Nghĩa: ${it.meaning_vi}\nĐộ tin cậy: ${(it.confidence * 100).toInt()}%",
                        Icons.Default.Info
                    )
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
fun JapaneseHomeScreenV2(onNavigate: (String) -> Unit) {
    var input by remember { mutableStateOf("日本語を勉強しています。") }
    var isJaVi by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item { HeroHeaderV2() }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionCardV2 {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.quick_analysis), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { isJaVi = !isJaVi }) {
                            Text(if (isJaVi) "JA -> VI" else "VI -> JA")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        input, { input = it },
                        Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.paste_hint)) },
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(translatePreviewV2(input, if (isJaVi) "ja_vi" else "vi_ja"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    if (isJaVi) {
                        Text(analyzePreviewV2(input), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.recommended_features), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolTileV2(Icons.Default.AutoStories, stringResource(R.string.learn_vocab), stringResource(R.string.jlpt_levels), { onNavigate(Screen.Flashcard.route) })
                    ToolTileV2(Icons.Default.Quiz, stringResource(R.string.do_exam), stringResource(R.string.jlpt_levels), { onNavigate(Screen.Quiz.route) })
                }
            }
        }
    }
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
    
    val authState by authViewModel.authState.collectAsState()
    val localDecks by viewModel.localDecks.collectAsState()
    val remoteDecks by viewModel.remoteDecks.collectAsState()
    val currentCards by viewModel.currentCards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
            Text(stringResource(R.string.studying), color = MaterialTheme.colorScheme.onSurfaceVariant)
            
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
                        items(localDecks) { deck ->
                            DeckCardV2(
                                title = deck.title,
                                cardCount = 0,
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
        
        ScreenScaffoldV2("Thêm thẻ mới", "Nhập thông tin cho thẻ flashcard.") {
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

        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                Text(stringResource(R.string.studying), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddCard = true }) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(40.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(350.dp).clickable { flipped = !flipped },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (flipped) card.back else card.front, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(12.dp))
                        Text(if (flipped) (card.example_sentence ?: "") else stringResource(R.string.tap_to_see_meaning), color = Color.White.copy(alpha = 0.75f))
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
private fun HeroHeaderV2() {
    Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(0.7f)))).padding(24.dp)) {
        Column(Modifier.align(Alignment.BottomStart)) {
            Text(stringResource(R.string.welcome_user), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(stringResource(R.string.home_subtitle), color = Color.White.copy(0.85f))
        }
        Surface(Modifier.align(Alignment.TopEnd).size(50.dp), shape = CircleShape, color = Color.White.copy(0.2f)) {
            Box(contentAlignment = Alignment.Center) { Text("日", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ScreenScaffoldV2(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        content()
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SectionCardV2(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun RowScope.ToolTileV2(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(Modifier.weight(1f).height(140.dp).clickable(onClick = onClick), shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Surface(Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DeckCardV2(title: String, cardCount: Int, onLearn: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(50.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Style, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("$cardCount ${stringResource(R.string.flashcards)}", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onLearn, shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.learn_now)) }
        }
    }
}

@Composable
private fun ServerDeckCardV2(title: String, onDownload: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text("Chia sẻ bởi cộng đồng", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDownload) { Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
private fun KanjiCandidateCard(kanji: String, confidence: Float, meaning: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(kanji, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("${(confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            Text(meaning, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UploadCardV2(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().height(140.dp).clickable(onClick = onClick), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(0.4f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f))) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ResultBlockV2(title: String, body: String, icon: ImageVector) {
    SectionCardV2 {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(body)
    }
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
