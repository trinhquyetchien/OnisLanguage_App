package com.onislanguage.app.ui.layout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
import com.onislanguage.app.utils.ThemeManager

@Composable
fun AppLayout(
    authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ServiceLocator.provideAuthViewModel() as T
        }
    })
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by rememberSaveable {
        mutableStateOf(ThemeManager.getDarkTheme(context, systemDark))
    }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val topLevelRoutes = setOf(
        Screen.Flashcard.route,
        Screen.Quiz.route,
        Screen.Dashboard.route,
        Screen.AiHub.route,
        Screen.Chat.route
    )
    val showBottomBar = currentRoute in topLevelRoutes

    OnisLanguageTheme(darkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavigationBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigateTopLevel(route)
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
                        JapaneseHomeScreenV2(
                            onNavigate = { navController.navigate(it) },
                            onOpenSettings = { navController.navigate(Screen.Settings.route) }
                        )
                    }
                    composable(Screen.AiHub.route) {
                        AiHubScreen(
                            onNavigate = { navController.navigate(it) },
                            onBack = {
                                navController.navigateTopLevel(Screen.Dashboard.route)
                            }
                        )
                    }
                    composable(Screen.AudioToText.route) {
                        AudioToTextScreenV2(onOpenResult = { historyId ->
                            navController.navigate(Screen.AudioToTextResult.createRoute(historyId))
                        })
                    }
                    composable(Screen.RecordToText.route) {
                        RecordToTextScreenV2(onOpenResult = { historyId ->
                            navController.navigate(Screen.AudioToTextResult.createRoute(historyId))
                        })
                    }
                    composable(Screen.AudioToTextResult.route) { backStackEntry ->
                        val historyId = backStackEntry.arguments?.getString("historyId")?.toLongOrNull()
                        AudioToTextResultScreenV2(
                            historyId = historyId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.ImageAnalysis.route) {
                        ImageAnalysisScreenV2(onOpenResult = { historyId ->
                            navController.navigate(Screen.ImageAnalysisResult.createRoute(historyId))
                        })
                    }
                    composable(Screen.ImageAnalysisResult.route) { backStackEntry ->
                        val historyId = backStackEntry.arguments?.getString("historyId")?.toLongOrNull()
                        ImageAnalysisResultScreenV2(
                            historyId = historyId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Kanji.route) { KanjiScreenV2() }
                    composable(Screen.Flashcard.route) { FlashcardScreenV2(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Import.route) { ImportScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Analysis.route) { AnalysisScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Vocabulary.route) { VocabularyScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Grammar.route) { GrammarScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Chat.route) { ChatScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Quiz.route) { QuizScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.CreateExam.route) { CreateExamScreen(onBack = { navController.popBackStack() }) }
                    composable(Screen.History.route) { HistoryScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Profile.route) { ProfileScreen(onNavigate = { navController.navigate(it) }) }
                    composable(Screen.Login.route) {
                        LoginScreenV2(onLoginSuccess = {
                            navController.popBackStack()
                        })
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = {
                                isDarkTheme = it
                                ThemeManager.setDarkTheme(context, it)
                            },
                            onNavigate = { route ->
                                if (route in topLevelRoutes) navController.navigateTopLevel(route)
                                else navController.navigate(route)
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }
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
        NavItem(Screen.Flashcard.route, stringResource(R.string.flashcards), Icons.Filled.Style),
        NavItem(Screen.Quiz.route, stringResource(R.string.quiz), Icons.Filled.Assignment),
        NavItem(Screen.Dashboard.route, stringResource(R.string.home), Icons.Filled.Home, isCenter = true),
        NavItem(Screen.AiHub.route, stringResource(R.string.ai_tools), Icons.Filled.SmartToy),
        NavItem(Screen.Chat.route, "Chatbot", Icons.Filled.ChatBubble)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route
                    val itemShape = if (item.isCenter) RoundedCornerShape(24.dp) else RoundedCornerShape(18.dp)
                    val lift by animateDpAsState(
                        targetValue = if (selected) (-3).dp else 0.dp,
                        animationSpec = spring(),
                        label = "menuLift"
                    )
                    val iconSize by animateDpAsState(
                        targetValue = if (selected) 24.dp else 20.dp,
                        animationSpec = spring(),
                        label = "menuIconSize"
                    )
                    val itemColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f) else Color.Transparent,
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
                            .padding(vertical = if (item.isCenter) 11.dp else 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (item.isCenter) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier
                                        .padding(9.dp)
                                        .size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
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

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(Screen.Dashboard.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
