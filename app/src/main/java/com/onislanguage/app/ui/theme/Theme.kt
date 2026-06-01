package com.onislanguage.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FBEF8),
    onPrimary = Color(0xFF08214A),
    primaryContainer = Color(0xFF173B73),
    onPrimaryContainer = Color(0xFFEAF2FF),
    secondary = Color(0xFFB7CCF2),
    onSecondary = Color(0xFF0E264F),
    secondaryContainer = Color(0xFF203E71),
    onSecondaryContainer = Color(0xFFEAF2FF),
    tertiary = Color(0xFFBFD3FF),
    onTertiary = Color(0xFF123468),
    tertiaryContainer = Color(0xFF2A5597),
    onTertiaryContainer = Color(0xFFEAF1FF),
    error = Color(0xFFFFB86A),
    onError = Color(0xFF2D1600),
    errorContainer = Color(0xFF6B3A00),
    onErrorContainer = Color(0xFFFFE1BF),
    background = Color(0xFF0A1730),
    onBackground = Color(0xFFEAF0FC),
    surface = Color(0xFF0F1F3B),
    onSurface = Color(0xFFEAF0FC),
    surfaceVariant = Color(0xFF1A2F53),
    onSurfaceVariant = Color(0xFFB8C9E5),
    outline = Color(0xFF46608D)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = ErrorColor,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    background = Background,
    onBackground = OnBackground
)

private val OnisTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.7).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.35.sp
    )
)

@Composable
fun OnisLanguageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            var currentContext = context
            while (currentContext is android.content.ContextWrapper) {
                if (currentContext is Activity) break
                currentContext = currentContext.baseContext
            }
            
            val activity = currentContext as? Activity
            activity?.window?.let { window ->
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OnisTypography,
        content = content
    )
}
