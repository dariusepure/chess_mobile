package com.dariusepure.chessmobile.ui

import androidx.compose.ui.graphics.Color

data class BoardTheme(
    val lightSquare: Color,
    val darkSquare: Color,
    val name: String
)

val ClassicTheme = BoardTheme(Color(0xFFEBECD0), Color(0xFF779556), "Classic")
val WoodTheme = BoardTheme(Color(0xFFDCAE73), Color(0xFFA56F42), "Wood")
val BlueTheme = BoardTheme(Color(0xFFDEE3E6), Color(0xFF8CA2AD), "Blue")
val GreenTheme = BoardTheme(Color(0xFFEEEED2), Color(0xFF769656), "Green")

val AllThemes = listOf(ClassicTheme, WoodTheme, BlueTheme, GreenTheme)
