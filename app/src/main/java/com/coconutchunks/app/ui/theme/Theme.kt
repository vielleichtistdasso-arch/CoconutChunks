package com.coconutchunks.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CoconutBrownDark = Color(0xFF5D4037)
val CoconutBrown = Color(0xFF795548)
val CoconutBrownSoft = Color(0xFF8D6E63)
val CoconutSand = Color(0xFFD7CCC8)
val CoconutCream = Color(0xFFFFF8F2)
val CoconutInk = Color(0xFF2D211C)

private val CoconutColorScheme = lightColorScheme(
    primary = CoconutBrownDark,
    onPrimary = Color.White,
    primaryContainer = CoconutSand,
    onPrimaryContainer = CoconutInk,
    secondary = CoconutBrown,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDD7),
    onSecondaryContainer = CoconutInk,
    tertiary = CoconutBrownSoft,
    onTertiary = Color.White,
    background = CoconutCream,
    onBackground = CoconutInk,
    surface = CoconutCream,
    onSurface = CoconutInk,
)

@Composable
fun CoconutChunksTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CoconutColorScheme,
        content = content,
    )
}
