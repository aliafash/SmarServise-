package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.example.data.AppConfig

object ThemeManager {

    // 1. Cosmic Slate (كوزميك سيلفر)
    val SlateBackground = Color(0xFF0F172A)
    val SlateSurface = Color(0xFF1E293B)
    val SlatePrimary = Color(0xFFE2E8F0)
    val SlateAccent = Color(0xFF38BDF8)
    val SlateSecondary = Color(0xFF64748B)

    // 2. Charcoal Gold (الذهبي الفاخر)
    val CharcoalBackground = Color(0xFF101011)
    val CharcoalSurface = Color(0xFF1B1B1C)
    val CharcoalPrimary = Color(0xFFD4AF37)
    val CharcoalAccent = Color(0xFFF3E5AB)
    val CharcoalSecondary = Color(0xFF4A4A4B)

    // 3. Royal Emerald (الزمردي الراقي)
    val EmeraldBackground = Color(0xFF061A13)
    val EmeraldSurface = Color(0xFF0D2D21)
    val EmeraldPrimary = Color(0xFF10B981)
    val EmeraldAccent = Color(0xFF6EE7B7)
    val EmeraldSecondary = Color(0xFF0F766E)

    // Font Colors
    val BrightWhite = Color(0xFFFFFFFF)
    val LightGold = Color(0xFFFFF1C5)
    val VibrantSilver = Color(0xFFD1D5DB)

    fun getColorScheme(config: AppConfig?): ColorScheme {
        val theme = config?.themeName ?: "COSMIC_SLATE"
        return when (theme) {
            "CHARCOAL_GOLD" -> darkColorScheme(
                primary = CharcoalPrimary,
                secondary = CharcoalAccent,
                background = CharcoalBackground,
                surface = CharcoalSurface,
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
            "ROYAL_EMERALD" -> darkColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldAccent,
                background = EmeraldBackground,
                surface = EmeraldSurface,
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
            else -> darkColorScheme( // COSMIC_SLATE (Default)
                primary = SlatePrimary,
                secondary = SlateAccent,
                background = SlateBackground,
                surface = SlateSurface,
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
        }
    }

    fun getFontColor(config: AppConfig?): Color {
        return when (config?.fontColorName ?: "BRIGHT_WHITE") {
            "LIGHT_GOLD" -> LightGold
            "VIBRANT_SILVER" -> VibrantSilver
            else -> BrightWhite
        }
    }
}
