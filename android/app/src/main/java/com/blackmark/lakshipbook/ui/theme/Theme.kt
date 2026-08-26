package com.blackmark.bloodlink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF171817)
private val Canvas = Color(0xFFF8F7F3)
private val Surface = Color(0xFFFFFFFF)
private val Coral = Color(0xFFC94747)
private val CoralDark = Color(0xFF8F2D32)
private val CoralSoft = Color(0xFFFCE8E5)
private val Green = Color(0xFF246B4A)
private val GreenSoft = Color(0xFFE3F2E8)
private val Muted = Color(0xFF6C706B)
private val Outline = Color(0xFFE2E3DE)

private val LightColors = lightColorScheme(
    primary = CoralDark,
    onPrimary = Color.White,
    primaryContainer = CoralSoft,
    onPrimaryContainer = Color(0xFF4A1014),
    secondary = Green,
    onSecondary = Color.White,
    secondaryContainer = GreenSoft,
    onSecondaryContainer = Color(0xFF0D3823),
    tertiary = Green,
    onTertiary = Color.White,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0F1EC),
    onSurfaceVariant = Muted,
    outline = Color(0xFF9B9E97),
    outlineVariant = Outline,
    error = CoralDark,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB3AD),
    onPrimary = Color(0xFF5F1117),
    primaryContainer = Color(0xFF7B252D),
    onPrimaryContainer = CoralSoft,
    secondary = Color(0xFFA6D5B7),
    onSecondary = Color(0xFF123B25),
    secondaryContainer = Color(0xFF245B3C),
    onSecondaryContainer = GreenSoft,
    tertiary = Color(0xFFA6D5B7),
    onTertiary = Color(0xFF123B25),
    background = Color(0xFF121412),
    onBackground = Color(0xFFE6E8E2),
    surface = Color(0xFF1A1D1A),
    onSurface = Color(0xFFE6E8E2),
    surfaceVariant = Color(0xFF292D29),
    onSurfaceVariant = Color(0xFFC0C5BD),
    outline = Color(0xFF8A9189),
    outlineVariant = Color(0xFF3C423C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun BloodLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
