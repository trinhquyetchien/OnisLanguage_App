package com.onislanguage.app.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard")
    object Import : Screen("import")
    object Analysis : Screen("analysis")
    object Vocabulary : Screen("vocabulary")
    object Grammar : Screen("grammar")
    object Chat : Screen("chat")
    object Quiz : Screen("quiz")
    object History : Screen("history")
    object Profile : Screen("profile")
}
