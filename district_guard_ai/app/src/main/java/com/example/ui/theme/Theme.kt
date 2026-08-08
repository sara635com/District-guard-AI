package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = HealthNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = HealthNavySecondary,
    onPrimaryContainer = Color.White,
    secondary = HealthBlueAccent,
    onSecondary = Color.White,
    background = HealthLightBg,
    onBackground = HealthTextPrimary,
    surface = HealthCardBg,
    onSurface = HealthTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = HealthTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = HealthNavyPrimary,
    primaryContainer = HealthNavySecondary,
    onPrimaryContainer = Color.White,
    secondary = HealthBlueAccent,
    onSecondary = Color.White,
    background = HealthNavyPrimary,
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun DistrictGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
