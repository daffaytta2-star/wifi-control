package com.axiom.wificontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SpeedTestTab(ctx: android.content.Context) {
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SpeedTest.Result?>(null) }
    var liveSpeed by remember { mutableStateOf(0.0) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.md)
    ) {

        // Title
        Text(
            "Speed Test",
            style = AppText.title,
            color = AppColor.TextPrimary,
            modifier = Modifier.padding(bottom = AppSpacing.lg)
        )

        // Card UNDUH + UNGGAH
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            SpeedCard(
                title = "Unduh",
                value = result?.downloadMbps ?: 0.0,
                accentColor = AppColor.Accent,
                modifier = Modifier.weight(1f)
            )
            SpeedCard(
                title = "Unggah",
                value = result?.uploadMbps ?: 0.0,
                accentColor = AppColor.Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(AppSpacing.lg))

        // Ping + Jitter
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem("Ping", (result?.pingMs ?: 0L).toString() + " ms")
            MetricItem("Jitter", "0 ms")
            MetricItem("Koneksi", if (running) "..." else "OK")
        }

        Spacer(Modifier.height(AppSpacing.xl))

        // Gauge
        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SpeedGauge(
                speedMbps = if (running) liveSpeed else (result?.downloadMbps ?: 0.0),
                maxSpeed = 100.0
            )
        }

        Spacer(Modifier.height(AppSpacing.md))

        // Status text
        Box(
            Modifier.fillMaxWidth().height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (running) {
                Text(progress, style = AppText.body, color = AppColor.TextSecondary)
            } else {
                Text(
                    if (result == null) "Tekan Mulai untuk tes kecepatan"
                    else "Tes selesai",
                    style = AppText.body,
                    color = AppColor.TextSecondary
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.lg))

        // Button
        PrimaryButton(
            text = if (running) "Mengukur..." else "Mulai Tes",
            onClick = {
                if (!running) {
                    scope.launch {
                        running = true
                        result = null
                        result = SpeedTest.runAll(
                            onProgress = { progress = it },
                            onLiveSpeed = { liveSpeed = it }
                        )
                        running = false
                        liveSpeed = 0.0
                    }
                }
            },
            enabled = !running,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SpeedCard(
    title: String,
    value: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(AppRadius.lg)
            .background(AppColor.Surface)
            .padding(AppSpacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor)
            )
            Spacer(Modifier.width(AppSpacing.sm))
            Text(
                title,
                style = AppText.body,
                color = AppColor.TextSecondary
            )
        }
        Spacer(Modifier.height(AppSpacing.md))
        Text(
            String.format("%.2f", value),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = AppColor.TextPrimary
        )
        Text(
            "Mbps",
            style = AppText.caption,
            color = AppColor.TextMuted
        )
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = AppText.bodyBold,
            color = AppColor.TextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = AppText.caption,
            color = AppColor.TextMuted
        )
    }
}
