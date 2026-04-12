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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Welcome.route

    val showBottomNav = currentRoute !in listOf(
        Screen.Welcome.route,
        Screen.Quiz.route,
        Screen.Chat.route
    )

    Scaffold(
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
            startDestination = Screen.Welcome.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Import.route) {
                ImportScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Vocabulary.route) {
                VocabularyScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Grammar.route) {
                GrammarScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Chat.route) {
                ChatScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Quiz.route) {
                QuizScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.History.route) {
                HistoryScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onNavigate = { navController.navigate(it) })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val navItems = listOf(
        NavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home),
        NavItem(Screen.Import.route, "Learn", Icons.Filled.School),
        NavItem(Screen.History.route, "History", Icons.Filled.History),
        NavItem(Screen.Chat.route, "AI Chat", Icons.Filled.SmartToy),
        NavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 40.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = Color(0xFF2D2E36).copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent

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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = contentColor
                        )
                    )
                }
            }
        }
    }
}

data class NavItem(val route: String, val label: String, val icon: ImageVector)
