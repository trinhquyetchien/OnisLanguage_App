package com.onislanguage.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onislanguage.app.navigation.Screen

@Composable
fun JapaneseHomeScreen(onNavigate: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
    ) {
        // Upper Header Section with Blue Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Konnichiwa! 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Bạn muốn học gì hôm nay?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("日", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                // Search Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text("Nhập Kanji hoặc từ vựng...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                            // Simplified for UI demo
                            Text(query, color = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { /* Search */ }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Dashboard Content
        Column(modifier = Modifier.padding(20.dp)) {
            // Quick Tools Section
            Text("Công cụ học tập", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                ToolItem(
                    icon = Icons.Default.GraphicEq,
                    title = "Audio",
                    color = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(Screen.AudioToText.route) }
                Spacer(modifier = Modifier.width(12.dp))
                ToolItem(
                    icon = Icons.Default.ImageSearch,
                    title = "Hình ảnh",
                    color = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF7B1FA2),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(Screen.ImageAnalysis.route) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ToolItem(
                    icon = Icons.Default.Create,
                    title = "Kanji",
                    color = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF388E3C),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(Screen.Kanji.route) }
                Spacer(modifier = Modifier.width(12.dp))
                ToolItem(
                    icon = Icons.Default.Style,
                    title = "Flashcards",
                    color = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(Screen.Flashcard.route) }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Latest Search Analysis (Mock)
            Text("Phân tích từ mới nhất", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            AnalysisCard("海 (Umi)", "Biển", "N5", "海が青いです。")

            Spacer(modifier = Modifier.height(32.dp))
            
            // Library Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thư viện từ vựng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = { }) {
                    Text("Xem tất cả", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item { 
                    LibraryItem(
                        "Giao thông", 
                        "120 từ", 
                        "https://images.unsplash.com/photo-15420518418c7-59496ba82752?auto=format&fit=crop&w=400&q=80"
                    ) 
                }
                item { 
                    LibraryItem(
                        "Nhà hàng", 
                        "85 từ", 
                        "https://images.unsplash.com/photo-1552056752-045336915104?auto=format&fit=crop&w=400&q=80"
                    ) 
                }
                item { 
                    LibraryItem(
                        "Trường học", 
                        "200 từ", 
                        "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=400&q=80"
                    ) 
                }
            }
        }
    }
}

@Composable
fun AudioToTextScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection("Audio to Text", "Chuyển đổi âm thanh sang văn bản tiếng Nhật")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Upload Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Color.Transparent)))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CloudUpload, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tải lên file Audio hoặc Video", fontWeight = FontWeight.Bold)
                Text("MP3, MP4, WAV tối đa 50MB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ghi âm trực tiếp")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Result Mock
        Text("Kết quả phân tích", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        ResultCard {
            Text(
                "こんにちは、今日はとてもいい天気ですね。一緒に公園へ行きませんか？",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Dịch: Chào bạn, hôm nay thời tiết thật đẹp nhỉ. Bạn có muốn cùng đi công viên không?", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ImageAnalysisScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection("Nhận diện hình ảnh", "Trích xuất và phân tích văn bản từ ảnh")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Image Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1545569341-9eb8b30979d9?auto=format&fit=crop&w=800&q=80",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Detection Overlay Mock
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Cyan.copy(alpha = 0.3f),
                    topLeft = Offset(100f, 150f),
                    size = androidx.compose.ui.geometry.Size(300f, 100f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thư viện")
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chụp ảnh")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Văn bản phát hiện", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        AnalysisCard("富士山 (Fujisan)", "Núi Phú Sĩ", "N4", "富士山は日本で一番高い山です。")
    }
}

@Composable
fun KanjiScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection("Luyện viết Kanji", "Vẽ và nhận diện chữ Hán")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Canvas Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 1.dp.toPx()
                    val color = Color.LightGray.copy(alpha = 0.5f)
                    drawLine(color, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                }
                
                // Mock Drawing
                Canvas(modifier = Modifier.fillMaxSize().padding(40.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.3f)
                        quadraticBezierTo(size.width * 0.5f, size.height * 0.25f, size.width * 0.8f, size.height * 0.3f)
                        moveTo(size.width * 0.5f, size.height * 0.3f)
                        lineTo(size.width * 0.5f, size.height * 0.8f)
                    }
                    drawPath(path, color = Color.Black, style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                
                Text(
                    "Hãy vẽ vào đây", 
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Xóa")
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Nhận diện")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Kết quả phù hợp", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KanjiResultItem("木", "Mộc", true)
            KanjiResultItem("本", "Bản", false)
            KanjiResultItem("林", "Lâm", false)
        }
    }
}

@Composable
fun FlashcardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection("Flashcards", "Ôn tập từ vựng một cách hiệu quả")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Flashcard Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background stack decoration
            Surface(
                modifier = Modifier.fillMaxWidth(0.85f).height(320.dp).offset(y = 20.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {}
            
            // Main Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(320.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "勉強",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Benkyou",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        "Chạm để xem nghĩa",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Thẻ số 12 / 45", fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(
                modifier = Modifier.size(64.dp).clickable { },
                shape = CircleShape,
                color = Color(0xFFFFEBEE),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                }
            }
            
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Tiếp theo", style = MaterialTheme.typography.titleMedium)
            }
            
            Surface(
                modifier = Modifier.size(64.dp).clickable { },
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}

// Reusable Components
@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ToolItem(icon: ImageVector, title: String, color: Color, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun AnalysisCard(kanji: String, meaning: String, level: String, example: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(kanji.take(1), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(kanji, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(meaning, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(level, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ví dụ:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(example, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun LibraryItem(title: String, count: String, imageUrl: String) {
    Column(modifier = Modifier.width(160.dp)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontWeight = FontWeight.Bold)
        Text(count, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ResultCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun KanjiResultItem(kanji: String, meaning: String, isSelected: Boolean) {
    Surface(
        modifier = Modifier
            .size(80.dp, 100.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                kanji, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                meaning, 
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
