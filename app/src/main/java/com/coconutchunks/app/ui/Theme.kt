package com.coconutchunks.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CoconutBrown = Color(0xFF68462F)
val CoconutBrownDark = Color(0xFF332116)
val CoconutCream = Color(0xFFFFF8EF)
val CoconutSurface = Color(0xFFFFFCF8)
val CoconutSand = Color(0xFFF2E4D4)
val CoconutLeaf = Color(0xFF617052)
val CoconutLeafSoft = Color(0xFFDDE6D2)
val CoconutSpecial = Color(0xFF8A5A16)
val CoconutSpecialSoft = Color(0xFFFFE7BA)
val CoconutMastered = Color(0xFF3D6847)
val CoconutMasteredSoft = Color(0xFFDCECDD)
val CoconutReview = Color(0xFF5E5B56)
val CoconutReviewSoft = Color(0xFFEAE6E0)

private val CoconutColors = lightColorScheme(
    primary = CoconutBrown,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4DECA),
    onPrimaryContainer = CoconutBrownDark,
    secondary = CoconutLeaf,
    onSecondary = Color.White,
    secondaryContainer = CoconutLeafSoft,
    onSecondaryContainer = Color(0xFF1F281C),
    background = CoconutCream,
    onBackground = CoconutBrownDark,
    surface = CoconutSurface,
    onSurface = CoconutBrownDark,
    surfaceVariant = CoconutSand,
    onSurfaceVariant = Color(0xFF665B52),
    outline = Color(0xFF8F8175),
    outlineVariant = Color(0xFFD8CBC0)
)

private val CoconutTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun CoconutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoconutColors,
        typography = CoconutTypography,
        content = content
    )
}
