package com.onislanguage.app.utils

import android.content.Context

object ThemeManager {
    private const val PREFS_NAME = "onis_ui_prefs"
    private const val KEY_DARK_THEME = "dark_theme"

    fun setDarkTheme(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_THEME, enabled)
            .apply()
    }

    fun getDarkTheme(context: Context, defaultValue: Boolean): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_THEME, defaultValue)
    }
}
