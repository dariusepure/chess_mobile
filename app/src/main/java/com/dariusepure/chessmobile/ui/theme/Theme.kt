package com.dariusepure.chessmobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ChessGreen,
    secondary = ChessSurface,
    tertiary = Pink80,
    background = ChessDarkBackground,
    surface = ChessSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ChessGreen,
    secondary = ChessSurface,
    tertiary = Pink40,
    background = Color(0xFFF1F1F1),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = ChessDarkBackground,
    onSurface = ChessDarkBackground
)

@Composable
fun ChessMobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
