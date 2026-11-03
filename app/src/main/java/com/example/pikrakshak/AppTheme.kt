
package com.example.pikrakshak

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF166534),
    onPrimary = Color.White,
    secondary = Color(0xFF22C55E),
    background = Color(0xFFF7FAF7),
    onBackground = Color(0xFF163321),
    surface = Color.White,
    onSurface = Color(0xFF163321),
    surfaceVariant = Color(0xFFE8F2E9),
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF86EFAC),
    onPrimary = Color(0xFF052E16),

    secondary = Color(0xFF6EE7B7),
    onSecondary = Color(0xFF062D22),

    tertiary = Color(0xFFA7F3D0),

    background = Color(0xFF0B1410),
    onBackground = Color(0xFFE8F5EC),

    surface = Color(0xFF121F18),
    onSurface = Color(0xFFE8F5EC),

    surfaceVariant = Color(0xFF24352B),
    onSurfaceVariant = Color(0xFFB5C9BA),

    outline = Color(0xFF53685A),
    error = Color(0xFFFF8A80)
)

@Composable
fun PikRakshakTheme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
