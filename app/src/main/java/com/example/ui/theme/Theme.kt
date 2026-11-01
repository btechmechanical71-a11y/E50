package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreen,
    onPrimary = Color.White,
    primaryContainer = DeepTeal,
    onPrimaryContainer = LimeSage,
    secondary = LimeSage,
    onSecondary = DeepTeal,
    secondaryContainer = LightSageDark,
    onSecondaryContainer = SoftCream,
    tertiary = SunGold,
    background = SoftCreamDark,
    onBackground = SoftCream,
    surface = SurfaceDark,
    onSurface = SoftCream,
    surfaceVariant = LightSageDark,
    onSurfaceVariant = LimeSage,
    outline = CardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = FreshGreen,
    onPrimary = Color.White,
    primaryContainer = LightSage,
    onPrimaryContainer = DeepTeal,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = LimeSage,
    onSecondaryContainer = DeepTeal,
    tertiary = SunGold,
    background = SoftCream,
    onBackground = DeepTeal,
    surface = Color.White,
    onSurface = DeepTeal,
    surfaceVariant = LightSage,
    onSurfaceVariant = DeepTeal,
    outline = CardBorder
)

@Composable
fun E50Theme(
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
