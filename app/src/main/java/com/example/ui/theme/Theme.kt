package com.example.ui.theme

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

private val AbmDarkColorScheme = darkColorScheme(
    primary = AbmCyanPrimary,
    onPrimary = AbmCyanOnPrimary,
    primaryContainer = AbmCyanContainer,
    onPrimaryContainer = AbmCyanOnContainer,
    secondary = AbmVioletSecondary,
    onSecondary = AbmVioletOnSecondary,
    secondaryContainer = AbmVioletContainer,
    onSecondaryContainer = AbmVioletOnContainer,
    tertiary = AbmElectricBlue,
    background = AbmDarkNavyBg,
    onBackground = AbmTextPrimary,
    surface = AbmDarkSurface,
    onSurface = AbmTextPrimary,
    surfaceVariant = AbmDarkSurfaceVariant,
    onSurfaceVariant = AbmTextSecondary,
    outline = AbmDarkBorder,
    error = AbmSeverityCritical,
    onError = Color.White
)

private val AbmLightColorScheme = lightColorScheme(
    primary = Color(0xFF007A8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F5FF),
    onPrimaryContainer = Color(0xFF002026),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E0FF),
    onSecondaryContainer = Color(0xFF1E1E4B),
    tertiary = Color(0xFF1565C0),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun ABMScanTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AbmDarkColorScheme else AbmLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for any template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ABMScanTheme(darkTheme = darkTheme, content = content)
}
