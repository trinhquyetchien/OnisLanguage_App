package com.onislanguage.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard")
    object AiHub : Screen("ai_hub")
    object AudioToText : Screen("audio_to_text")
    object ImageAnalysis : Screen("image_analysis")
    object Kanji : Screen("kanji")
    object Flashcard : Screen("flashcard")
    object Import : Screen("import")
    object Analysis : Screen("analysis")
    object Vocabulary : Screen("vocabulary")
    object Grammar : Screen("grammar")
    object Chat : Screen("chat")
    object Quiz : Screen("quiz")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}
