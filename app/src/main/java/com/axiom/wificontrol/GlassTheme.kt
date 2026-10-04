package com.axiom.wificontrol

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object DarkGlassColors {
    val BgBase = Color(0xFF0A0A0C)
    val BgSoft = Color(0xFF141418)

    // Card glass — lebih tebal, tapi tetap gelap
    val GlassTop = Color(0x4DFFFFFF)
    val GlassMid = Color(0x33FFFFFF)
    val GlassBot = Color(0x1AFFFFFF)
    val GlassBorder = Color(0x55FFFFFF)
    val GlassBorderSoft = Color(0x33FFFFFF)

    // Pill nav
    val PillBg = Color(0x22FFFFFF)
    val PillActive = Color(0x40FFFFFF)
    val PillBorder = Color(0x44FFFFFF)

    // Accent — putih/silver, bukan ungu
    val Accent = Color(0xFFE0E4EE)
    val AccentSoft = Color(0xFFB8BEC9)
    val Connect = Color(0xFF5DD97A)
    val Disconnect = Color(0xFFFF6B6B)
    val TextPrimary = Color(0xFFF0F2F8)
    val TextSecondary = Color(0xFF8B91A7)
}

@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = DarkGlassColors.Accent,
        secondary = DarkGlassColors.AccentSoft,
        background = DarkGlassColors.BgBase,
        surface = Color(0x33FFFFFF),
        onPrimary = Color(0xFF1A1D26),
        onSurface = DarkGlassColors.TextPrimary
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun Modifier.liquidBackground(): Modifier {
    return this.background(
        Brush.verticalGradient(
            0f to DarkGlassColors.BgSoft,
            1f to DarkGlassColors.BgBase
        )
    )
}

/** Liquid glass — blur Android 12+, fallback alpha Android 11. */
fun Modifier.glass(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp)
): Modifier {
    val base = this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                0f to DarkGlassColors.GlassTop,
                0.5f to DarkGlassColors.GlassMid,
                1f to DarkGlassColors.GlassBot
            ),
            shape
        )
        .border(BorderStroke(1.dp, DarkGlassColors.GlassBorder), shape)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        base.blur(24.dp)
    } else {
        base
    }
}

/** Pill transparan untuk bottom nav. */

fun Modifier.glassPill(
    shape: RoundedCornerShape = RoundedCornerShape(50),
    blurRadius: Int = 30,
    alphaValue: Int = 16
): Modifier {
    val bgColor = Color.argb(alphaValue, 255, 255, 255)
    val base = this
        .clip(shape)
        .background(bgColor, shape)
        .border(BorderStroke(1.dp, DarkGlassColors.PillBorder), shape)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        base.blur(blurRadius.dp)
    } else {
        base
    }
}
