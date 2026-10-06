package com.axiom.wificontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun HeroCard(
    netInfo: NetworkScanner.NetInfo,
    rooted: Boolean,
    onOpenRouter: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.padding(bottom = AppSpacing.md),
        padding = AppSpacing.lg
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (rooted) AppColor.Success else AppColor.Warning)
            )
            Spacer(Modifier.width(AppSpacing.sm))
            Text(
                "Jaringan Saya",
                style = AppText.section,
                color = AppColor.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(
                if (rooted) "ROOT" else "NO ROOT",
                if (rooted) AppColor.Success else AppColor.Warning,
                if (rooted) AppColor.SuccessBg else AppColor.DangerBg
            )
        }

        Spacer(Modifier.height(AppSpacing.md))
        AppDivider()
        Spacer(Modifier.height(AppSpacing.md))

        InfoRow("IP Address", netInfo.myIp)
        Spacer(Modifier.height(AppSpacing.sm))
        InfoRow("Gateway", netInfo.gatewayIp)
        Spacer(Modifier.height(AppSpacing.sm))
        InfoRow("Subnet", netInfo.subnet)

        Spacer(Modifier.height(AppSpacing.lg))

        PrimaryButton(
            text = "Buka Admin Router",
            onClick = onOpenRouter,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = AppText.body,
            color = AppColor.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = AppText.bodyBold,
            color = AppColor.TextPrimary
        )
    }
}
