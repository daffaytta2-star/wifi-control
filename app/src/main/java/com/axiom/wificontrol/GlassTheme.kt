package com.axiom.wificontrol

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object SilverColors {
    // Background — silver gradient
    val BgTop = Color(0xFFE8EBF2)
    val BgMid = Color(0xFFD5DAE6)
    val BgBot = Color(0xFFBFC5D4)

    // Glass — putih transparan
    val GlassTop = Color(0xCCFFFFFF)
    val GlassMid = Color(0x99FFFFFF)
    val GlassBot = Color(0x55FFFFFF)
    val GlassBorder = Color(0x88FFFFFF)

    // Accent — silver-blue
    val Accent = Color(0xFF6B7BA8)
    val AccentSoft = Color(0xFF8A97BE)
    val Connect = Color(0xFF4CAF50)
    val Disconnect = Color(0xFFE53935)
    val TextPrimary = Color(0xFF1A1D26)
    val TextSecondary = Color(0xFF5A6178)
}

@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = SilverColors.Accent,
        secondary = SilverColors.AccentSoft,
        background = SilverColors.BgBot,
        surface = Color(0xCCFFFFFF),
        onPrimary = Color.White,
        onSurface = SilverColors.TextPrimary
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun Modifier.liquidBackground(): Modifier {
    val brush = Brush.verticalGradient(
        0f to SilverColors.BgTop,
        0.5f to SilverColors.BgMid,
        1f to SilverColors.BgBot
    )
    return this.background(brush)
}

/** Efek liquid glass — putih transparan + border + blur (Android 12+). */
fun Modifier.glass(
    shape: RoundedCornerShape = RoundedCornerShape(20.dp)
): Modifier {
    val base = this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                0f to SilverColors.GlassTop,
                0.5f to SilverColors.GlassMid,
                1f to SilverColors.GlassBot
            ),
            shape
        )
        .border(BorderStroke(1.dp, SilverColors.GlassBorder), shape)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        base.blur(16.dp)
    } else {
        base
    }
}
