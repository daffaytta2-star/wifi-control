package com.axiom.wificontrol

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object GlassColors {
    val BgTop = Color(0xFF1B1B2F)
    val BgMid = Color(0xFF162447)
    val BgBot = Color(0xFF0F0F1A)

    val GlassTop = Color(0x55FFFFFF)
    val GlassMid = Color(0x33FFFFFF)
    val GlassBot = Color(0x22FFFFFF)
    val GlassBorder = Color(0x66FFFFFF)

    val Accent = Color(0xFF9C6BFF)
    val AccentSoft = Color(0xFFB794FF)
    val Connect = Color(0xFF4CD964)
    val Disconnect = Color(0xFFFF4C4C)
}

@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) darkColorScheme(
        primary = GlassColors.Accent,
        secondary = GlassColors.AccentSoft,
        background = GlassColors.BgBot,
        surface = Color(0x33FFFFFF),
        onPrimary = Color.White,
        onSurface = Color.White
    ) else lightColorScheme(
        primary = GlassColors.Accent,
        secondary = GlassColors.AccentSoft,
        background = Color(0xFFEEF1F8),
        surface = Color(0xAAFFFFFF),
        onPrimary = Color.White
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun Modifier.liquidBackground(): Modifier {
    val brush = Brush.verticalGradient(
        0f to GlassColors.BgTop,
        0.5f to GlassColors.BgMid,
        1f to GlassColors.BgBot
    )
    return this.background(brush)
}

fun Modifier.glass(
    shape: RoundedCornerShape = RoundedCornerShape(20.dp)
): Modifier {
    val base = this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                0f to GlassColors.GlassTop,
                0.5f to GlassColors.GlassMid,
                1f to GlassColors.GlassBot
            ),
            shape
        )
        .border(BorderStroke(1.dp, GlassColors.GlassBorder), shape)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        base.blur(20.dp)
    } else {
        base
    }
}
