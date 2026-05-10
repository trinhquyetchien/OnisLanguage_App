package com.onislanguage.app.ui.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onislanguage.app.R
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.navigation.Screen
import com.onislanguage.app.ui.screens.*
import com.onislanguage.app.ui.theme.OnisLanguageTheme
import com.onislanguage.app.ui.viewmodel.AuthViewModel

@Composable
fun AppLayout(
    authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val isAuthenticated = authState != null
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    OnisLanguageTheme(darkTheme = isDarkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(Screen.Dashboard.route) {
                    JapaneseHomeScreenV2(onNavigate = { navController.navigate(it) })
                }
                composable(Screen.AiHub.route) {
                    AiHubScreen(
                        onNavigate = { navController.navigate(it) },
                        onBack = {
                            navController.navigate(Screen.Dashboard.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.AudioToText.route) { AudioToTextScreenV2() }
                composable(Screen.ImageAnalysis.route) { ImageAnalysisScreenV2() }
                composable(Screen.Kanji.route) { KanjiScreenV2() }
                composable(Screen.Flashcard.route) { FlashcardScreenV2(onNavigate = { navController.navigate(it) }) }
                composable(Screen.Quiz.route) { QuizScreen(onNavigate = { navController.navigate(it) }) }
                composable(Screen.Login.route) {
                    LoginScreenV2(onLoginSuccess = {
                        navController.popBackStack()
                    })
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it },
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val navItems = listOf(
        NavItem(Screen.AiHub.route, stringResource(R.string.ai_tools), Icons.Filled.SmartToy),
        NavItem(Screen.Flashcard.route, stringResource(R.string.flashcards), Icons.Filled.Style),
        NavItem(Screen.Dashboard.route, stringResource(R.string.home), Icons.Filled.Home, isCenter = true),
        NavItem(Screen.Quiz.route, stringResource(R.string.quiz), Icons.Filled.Assignment),
        NavItem(Screen.Settings.route, stringResource(R.string.settings), Icons.Filled.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 10.dp,
            shadowElevation = 18.dp
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.route
                val itemShape = if (item.isCenter) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)
                val lift by animateDpAsState(
                    targetValue = if (selected) (-7).dp else 0.dp,
                    animationSpec = spring(),
                    label = "menuLift"
                )
                val iconSize by animateDpAsState(
                    targetValue = if (selected) 25.dp else 22.dp,
                    animationSpec = spring(),
                    label = "menuIconSize"
                )
                val itemColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    animationSpec = spring(),
                    label = "menuItemColor"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = lift)
                        .clip(itemShape)
                        .clickable { onNavigate(item.route) }
                        .background(itemColor)
                        .padding(vertical = if (item.isCenter) 10.dp else 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (item.isCenter) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(if (selected) 26.dp else 24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false
)
