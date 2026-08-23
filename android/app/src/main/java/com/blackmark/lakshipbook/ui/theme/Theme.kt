package com.blackmark.lakshipbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF10100F)
private val Charcoal = Color(0xFF1D1D1A)
private val Paper = Color(0xFFF5F0E8)
private val Bone = Color(0xFFE7DED0)
private val BlackmarkRed = Color(0xFF9A3E38)
private val RedDark = Color(0xFF7F2E2A)
private val Muted = Color(0xFFB7AA9A)

private val DarkColors = darkColorScheme(
    primary = Bone,
    onPrimary = Ink,
    primaryContainer = BlackmarkRed,
    onPrimaryContainer = Color.White,
    secondary = BlackmarkRed,
    onSecondary = Color.White,
    secondaryContainer = Charcoal,
    onSecondaryContainer = Paper,
    background = Ink,
    onBackground = Paper,
    surface = Charcoal,
    onSurface = Paper,
    surfaceVariant = Color(0xFF2A2925),
    onSurfaceVariant = Muted,
    outline = Color(0xFF5C564F),
    error = Color(0xFFF39A8F),
    onError = Ink
)

private val LightColors = lightColorScheme(
    primary = RedDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD5),
    onPrimaryContainer = Color(0xFF3B0807),
    secondary = Color(0xFF675D55),
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Bone,
    onSurfaceVariant = Color(0xFF4D4640),
    outline = Color(0xFF7D746B)
)

@Composable
fun LakShipBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
