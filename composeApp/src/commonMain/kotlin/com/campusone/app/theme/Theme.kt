package com.campusone.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IndigoPrimary = Color(0xFF4F46E5)
val NavyDark = Color(0xFF0D1127)
val CanvasBg = Color(0xFFF8FAFC)
val SurfaceWhite = Color(0xFFFFFFFF)

// Status Tokens matching UI Skeleton
val StatusGoodGreen = Color(0xFF10B981)     // ●
val StatusAttentionAmber = Color(0xFFF59E0B) // ▲
val StatusDangerRed = Color(0xFFEF4444)      // ■
val StatusInfoIndigo = Color(0xFF6366F1)     // ◆

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = NavyDark,
    background = CanvasBg,
    surface = SurfaceWhite,
    onSurface = Color(0xFF0F172A),
    onBackground = Color(0xFF0F172A),
)

@Composable
fun CampusOneTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}

