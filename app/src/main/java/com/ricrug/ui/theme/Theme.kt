package com.ricrug.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CosmicColorScheme =
  darkColorScheme(
    primary = CosmicAmber,
    onPrimary = ComicInk,
    primaryContainer = Color(0xFF473200),
    onPrimaryContainer = Color(0xFFFFDF9E),
    secondary = CosmicCyan,
    onSecondary = ComicInk,
    secondaryContainer = Color(0xFF003B47),
    onSecondaryContainer = Color(0xFFB3F7FF),
    tertiary = NebulaMagenta,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4D0D2A),
    onTertiaryContainer = Color(0xFFFFD6E5),
    background = SpaceVoid,
    onBackground = StarlightWhite,
    surface = SpaceNavy,
    onSurface = StarlightWhite,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = StarlightMuted,
    surfaceContainerHigh = SpaceCardElevated,
    outline = SpaceOutline,
  )

val CosmicShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
  )

@Composable
fun AstroViaggioTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = CosmicColorScheme,
    typography = AppTypography,
    shapes = CosmicShapes,
    content = content,
  )
}
