package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val HindSiliguriFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.noto_sans_bengali, FontWeight.Medium)
)

val DisplayFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium)
)

fun createAppTypography(scaleMultiplier: Float = 1.0f): Typography {
    fun s(base: Float) = (base * scaleMultiplier).sp
    return Typography(
        displaySmall = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = s(30f),
            lineHeight = s(38f)
        ),
        headlineMedium = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = s(24f),
            lineHeight = s(32f)
        ),
        headlineSmall = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(20f),
            lineHeight = s(28f)
        ),
        titleLarge = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(18f),
            lineHeight = s(26f)
        ),
        titleMedium = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(16f),
            lineHeight = s(24f)
        ),
        titleSmall = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.Medium,
            fontSize = s(14f),
            lineHeight = s(20f)
        ),
        bodyLarge = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.Normal,
            fontSize = s(15f),
            lineHeight = s(23f)
        ),
        bodyMedium = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.Normal,
            fontSize = s(14f),
            lineHeight = s(21f)
        ),
        bodySmall = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.Normal,
            fontSize = s(12f),
            lineHeight = s(18f)
        ),
        labelLarge = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = s(14f),
            lineHeight = s(20f)
        ),
        labelMedium = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.Medium,
            fontSize = s(12f),
            lineHeight = s(16f)
        ),
        labelSmall = TextStyle(
            fontFamily = HindSiliguriFamily,
            fontWeight = FontWeight.Medium,
            fontSize = s(11f),
            lineHeight = s(14f)
        )
    )
}

val Typography = createAppTypography(1.0f)
