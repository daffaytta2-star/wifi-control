package com.axiom.wificontrol

import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class DeviceMonitor(private val ctx: Context) {

    private var job: Job? = null
    private val knownMacs = mutableSetOf<String>()
    private val scanner = NetworkScanner(ctx)

    fun start() {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                try {
                    val devices = scanner.scan()
                    for (d in devices) {
                        val key = if (d.mac.contains("?")) d.ip else d.mac.lowercase()
                        if (knownMacs.isNotEmpty() && !knownMacs.contains(key)) {
                            notifyNewDevice(d)
                        }
                        knownMacs.add(key)
                    }
                } catch (_: Exception) {}
                delay(30_000)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun notifyNewDevice(d: Device) {
        val channelId = "wifi_new_device"
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId, "Device Baru",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(ch)
        }

        val notif = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Device baru nyambung WiFi")
            .setContentText("${d.ip} | ${d.vendor} | ${d.mac}")
            .setAutoCancel(true)
            .build()

        nm.notify(d.ip.hashCode(), notif)
    }
}
