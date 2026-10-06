package com.axiom.wificontrol

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// COLORS — Dark Navy + Blue Accent
// ============================================================
object AppColor {
    // Background
    val BgTop = Color(0xFF161B2C)
    val BgMid = Color(0xFF121620)
    val BgBottom = Color(0xFF0B0E16)

    // Surface
    val Surface = Color(0xFF1E2436)
    val SurfaceElevated = Color(0xFF252C42)
    val SurfaceBorder = Color(0xFF2E3651)

    // Accent
    val Accent = Color(0xFF6B8AFF)
    val AccentLight = Color(0xFF8BA4FF)
    val AccentDark = Color(0xFF4A6BDB)

    // Status
    val Success = Color(0xFF4ADE80)
    val SuccessBg = Color(0x224ADE80)
    val Danger = Color(0xFFF87171)
    val DangerBg = Color(0x22F87171)
    val Warning = Color(0xFFFBBF24)

    // Text
    val TextPrimary = Color(0xFFF1F4FA)
    val TextSecondary = Color(0xFF9CA5BF)
    val TextMuted = Color(0xFF6B7391)

    // Divider
    val Divider = Color(0xFF232A3E)
}

// ============================================================
// SPACING — Grid 4dp
// ============================================================
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// ============================================================
// RADIUS
// ============================================================
object AppRadius {
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(20.dp)
    val xxl = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

// ============================================================
// TYPOGRAPHY
// ============================================================
object AppText {
    val hero = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
    val section = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
    val body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
    val bodyBold = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
    val label = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp)
}

// ============================================================
// THEME
// ============================================================
private val AppColorScheme = darkColorScheme(
    primary = AppColor.Accent,
    onPrimary = Color.White,
    secondary = AppColor.AccentLight,
    background = AppColor.BgBottom,
    surface = AppColor.Surface,
    onSurface = AppColor.TextPrimary,
    error = AppColor.Danger,
    onError = Color.White
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography(),
        content = content
    )
}
