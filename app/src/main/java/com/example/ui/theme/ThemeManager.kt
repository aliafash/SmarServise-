package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.AppConfig

object ThemeManager {

    // Themes
    private val SilverColorScheme = darkColorScheme(
        primary = Color(0xFFC0C0C0), // Silver
        secondary = Color(0xFFE2E8F0), // Slate Light
        tertiary = Color(0xFF708090), // Slate Blue
        background = Color(0xFF0F172A), // Cosmic Dark Slate
        surface = Color(0xFF1E293B),
        onPrimary = Color(0xFF111827),
        onSecondary = Color(0xFF1E293B),
        onBackground = Color(0xFFF1F5F9),
        onSurface = Color(0xFFF1F5F9)
    )

    private val GoldColorScheme = darkColorScheme(
        primary = Color(0xFFD4AF37), // Metallic Gold
        secondary = Color(0xFFF3E5AB), // Mellow Gold/Amber
        tertiary = Color(0xFFAA7C11),
        background = Color(0xFF121212), // Dark Obsidian
        surface = Color(0xFF1A1A1A),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFFFFDD0),
        onSurface = Color(0xFFFFFDD0)
    )

    private val EmeraldColorScheme = darkColorScheme(
        primary = Color(0xFF50C878), // Emerald Green
        secondary = Color(0xFF98FF98),
        tertiary = Color(0xFF046307),
        background = Color(0xFF06140A), // Extremely Dark Deep Forest
        surface = Color(0xFF0E2214),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFE0F2E9),
        onSurface = Color(0xFFE0F2E9)
    )

    fun getColorScheme(config: AppConfig?): ColorScheme {
        return when (config?.colorThemeId) {
            "silver" -> SilverColorScheme
            "gold" -> GoldColorScheme
            "emerald" -> EmeraldColorScheme
            else -> SilverColorScheme
        }
    }

    fun getFontColor(config: AppConfig?): Color {
        return try {
            val hex = config?.fontColor ?: "#FFFFFF"
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.White
        }
    }

    fun getFontFamily(config: AppConfig?): FontFamily {
        return when (config?.fontTypeFace) {
            "monospace" -> FontFamily.Monospace
            else -> FontFamily.Default
        }
    }

    fun getFontWeight(config: AppConfig?): FontWeight {
        return when (config?.fontTypeFace) {
            "bold" -> FontWeight.Bold
            else -> FontWeight.Bold // User wanted prominent thick font weights ("خط عريض")
        }
    }

    fun getFontScale(config: AppConfig?): androidx.compose.ui.unit.TextUnit {
        val baseOffset = config?.fontSizeOffset ?: 0
        return (14 + baseOffset).sp
    }
}
