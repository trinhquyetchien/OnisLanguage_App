package com.onislanguage.app.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.ui.screens.*

@Composable
fun AppLayout() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    
    var isDarkTheme by remember { mutableStateOf(false) }

    OnisLanguageTheme(darkTheme = isDarkTheme) {
        val showBottomNav = currentRoute in mainRoutes

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomNav) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(Screen.Welcome.route) {
                    WelcomeScreen(onNavigate = { navController.navigate(it) })
                }
                composable(Screen.Dashboard.route) {
                    JapaneseHomeScreen(onNavigate = { navController.navigate(it) })
                }
                composable(Screen.AudioToText.route) {
                    AudioToTextScreen()
                }
                composable(Screen.ImageAnalysis.route) {
                    ImageAnalysisScreen()
                }
                composable(Screen.Kanji.route) {
                    KanjiScreen()
                }
                composable(Screen.Flashcard.route) {
                    FlashcardScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it },
                        onLogout = { /* Xử lý logout mock */ }
                    )
                }
                // ... rest of composables
            }
        }
    }
}

private val mainRoutes = listOf(
    Screen.Dashboard.route,
    Screen.AudioToText.route,
    Screen.ImageAnalysis.route,
    Screen.Kanji.route,
    Screen.Flashcard.route,
    Screen.Settings.route
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val navItems = listOf(
        NavItem(Screen.Dashboard.route, "Khám phá", Icons.Filled.Explore),
        NavItem(Screen.AudioToText.route, "Âm thanh", Icons.Filled.GraphicEq),
        NavItem(Screen.Kanji.route, "Luyện viết", Icons.Default.Create),
        NavItem(Screen.Flashcard.route, "Thẻ học", Icons.Default.Style),
        NavItem(Screen.Settings.route, "Cài đặt", Icons.Default.Settings)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(item.route) }
                        .background(backgroundColor)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(val route: String, val label: String, val icon: ImageVector)
