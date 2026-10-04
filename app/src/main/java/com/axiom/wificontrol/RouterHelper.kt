package com.axiom.wificontrol

import android.content.Context
import android.content.Intent
import android.net.Uri

object RouterHelper {
    fun openRouter(ctx: Context, routerIp: String = "192.168.1.1") {
        val url = if (routerIp.isBlank()) "http://192.168.1.1" else "http://$routerIp"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }
}
