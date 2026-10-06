package com.axiom.wificontrol

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    speedMbps: Double,
    maxSpeed: Double = 100.0,
    modifier: Modifier = Modifier
) {
    // Animasi jarum
    val targetAngle = (speedMbps / maxSpeed).coerceIn(0.0, 1.0) * 180.0
    val animatedSweep by animateFloatAsState(
        targetValue = targetAngle.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "gauge"
    )

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val padding = stroke / 2 + 4.dp.toPx()
            val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
            val topLeft = Offset(padding, padding)

            // Background arc (abu)
            drawArc(
                color = AppColor.SurfaceBorder,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc (accent)
            drawArc(
                color = AppColor.Accent,
                startAngle = 180f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Jarum
            val centerX = size.width / 2
            val centerY = size.height / 2
            val needleLength = (size.width / 2) - padding - 4.dp.toPx()
            val angleRad = Math.toRadians((180f + animatedSweep).toDouble())

            drawLine(
                color = AppColor.TextPrimary,
                start = Offset(centerX, centerY),
                end = Offset(
                    (centerX + needleLength * cos(angleRad)).toFloat(),
                    (centerY + needleLength * sin(angleRad)).toFloat()
                ),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Titik tengah
            drawCircle(
                color = AppColor.Accent,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Angka speed di tengah bawah
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                String.format("%.1f", speedMbps),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextPrimary
            )
            Text(
                "Mbps",
                fontSize = 12.sp,
                color = AppColor.TextMuted
            )
        }

        // Label 0 dan maxSpeed
        Box(Modifier.fillMaxSize()) {
            Text(
                "0",
                fontSize = 10.sp,
                color = AppColor.TextMuted,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 20.dp)
            )
            Text(
                maxSpeed.toInt().toString(),
                fontSize = 10.sp,
                color = AppColor.TextMuted,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 20.dp)
            )
        }
    }
}
