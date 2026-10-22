package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = Color.White,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal700,
    tertiary = Amber500,
    onTertiary = Color.White,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber600,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300,
    outlineVariant = Slate200,
    error = DangerRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Navy100,
    onPrimary = Navy900,
    primaryContainer = Navy800,
    onPrimaryContainer = Color.White,
    secondary = Teal500,
    onSecondary = Slate900,
    secondaryContainer = Teal700,
    onSecondaryContainer = Teal100,
    tertiary = Amber500,
    onTertiary = Slate900,
    tertiaryContainer = Amber600,
    onTertiaryContainer = Amber100,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    outline = Slate600,
    outlineVariant = Slate700,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun EduAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep custom school palette consistent for high brand identity
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
