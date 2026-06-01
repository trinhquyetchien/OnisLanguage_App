package com.onislanguage.app.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {
    private const val PREFS_NAME = "onis_ui_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun setLanguage(context: Context, languageCode: String) {
        val normalizedCode = normalizeLanguageCode(languageCode)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, normalizedCode)
            .apply()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(normalizedCode)
        } else {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(normalizedCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun applySavedLanguage(context: Context) {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
            ?: return
        setLanguage(context, saved)
    }

    fun getLanguage(context: Context): String {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
        if (!stored.isNullOrBlank()) return normalizeLanguageCode(stored)

        val fromSystem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales.toLanguageTags()
                .ifEmpty { "en" }
        } else {
            AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "en" }
        }
        return normalizeLanguageCode(fromSystem)
    }

    fun normalizeLanguageCode(languageCode: String?): String {
        val code = languageCode.orEmpty().lowercase()
        return when {
            code.startsWith("ja") -> "ja"
            code.startsWith("vi") -> "vi"
            code.startsWith("en") -> "en"
            else -> "en"
        }
    }
}
