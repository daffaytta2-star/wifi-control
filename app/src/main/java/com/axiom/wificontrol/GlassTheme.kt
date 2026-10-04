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
    val BgSoft = Color(0xFF121216)

    // Glass lebih tebal — alpha naik
    val GlassTop = Color(0x66FFFFFF)
    val GlassMid = Color(0x44FFFFFF)
    val GlassBot = Color(0x22FFFFFF)
    val GlassBorder = Color(0x77FFFFFF)
    val GlassBorderSoft = Color(0x44FFFFFF)

    val Accent = Color(0xFFB794FF)
    val AccentSoft = Color(0xFF9C6BFF)
    val Connect = Color(0xFF4CD964)
    val Disconnect = Color(0xFFFF5A5A)
    val TextPrimary = Color(0xFFF0F2F8)
    val TextSecondary = Color(0xFF8B91A7)
}

@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = DarkGlassColors.Accent,
        secondary = DarkGlassColors.AccentSoft,
        background = DarkGlassColors.BgBase,
        surface = Color(0x44FFFFFF),
        onPrimary = Color.White,
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

/** Liquid glass tebal — alpha tinggi, border putih, blur Android 12+. */
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
