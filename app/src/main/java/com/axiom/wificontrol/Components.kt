package com.axiom.wificontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// APP CARD — card utama dengan border + solid surface
// ============================================================
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    padding: Dp = AppSpacing.lg,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppRadius.lg)
            .background(AppColor.Surface)
            .border(1.dp, AppColor.SurfaceBorder, AppRadius.lg)
            .padding(padding),
        content = content
    )
}

// ============================================================
// PRIMARY BUTTON — solid accent
// ============================================================
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(AppRadius.md)
            .background(
                if (enabled) AppColor.Accent
                else AppColor.SurfaceBorder
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Color.White else AppColor.TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================================
// SECTION HEADER
// ============================================================
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = AppText.section,
            color = AppColor.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        action?.invoke()
    }
}

// ============================================================
// STATUS BADGE — pill kecil
// ============================================================
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(AppRadius.pill)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================================
// VENDOR AVATAR — bulat dengan inisial
// ============================================================
@Composable
fun VendorAvatar(
    vendor: String,
    size: Dp = 40.dp
) {
    val letter = vendor.take(1).uppercase().ifBlank { "?" }
    val color = when (vendor.lowercase()) {
        "apple" -> Color(0xFF8E8E93)
        "samsung" -> Color(0xFF1A73E8)
        "xiaomi" -> Color(0xFFFF6900)
        "google" -> Color(0xFF4285F4)
        "tp-link" -> Color(0xFF4ACBD6)
        "intel" -> Color(0xFF0071C5)
        else -> AppColor.Accent
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter,
            color = color,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// DIVIDER
// ============================================================
@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColor.Divider)
    )
}
