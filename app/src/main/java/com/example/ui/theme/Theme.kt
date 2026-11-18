package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BrandDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepMidnight,
    primaryContainer = DeepElevated,
    onPrimaryContainer = CyanGlow,
    secondary = CobaltBlue,
    onSecondary = Color.White,
    secondaryContainer = DeepCard,
    onSecondaryContainer = SkyBlue,
    tertiary = AmberHighlight,
    onTertiary = DeepMidnight,
    background = DeepMidnight,
    onBackground = TextPrimaryDark,
    surface = DeepNavy,
    onSurface = TextPrimaryDark,
    surfaceVariant = DeepCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DeepBorder,
    error = CoralAlert,
    onError = DeepMidnight
)

private val BrandLightColorScheme = lightColorScheme(
    primary = DeepRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = DeepRoyalBlue,
    secondary = CyanTealDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6F7FF),
    onSecondaryContainer = Color(0xFF003642),
    tertiary = AmberHighlight,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFC2D5EE),
    error = Color(0xFFDC2626),
    onError = Color.White
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KabirTechLabTheme(
    themeMode: String = "dark",
    fontSizePreference: String = "medium",
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "light" -> false
        "system" -> isSystemInDarkTheme()
        else -> true
    }
    val scale = when (fontSizePreference) {
        "small" -> 0.9f
        "large" -> 1.14f
        else -> 1.0f
    }

    MaterialTheme(
        colorScheme = if (useDark) BrandDarkColorScheme else BrandLightColorScheme,
        typography = createAppTypography(scale),
        shapes = AppShapes,
        content = content
    )
}
