package com.axiom.wificontrol

import java.io.DataOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

object RootShell {

    // Cari binary su di semua path yang mungkin
    private val SU_PATHS = listOf(
        "/debug_ramdisk/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/adb/ksu/bin/su",
        "/data/adb/magisk/su",
        "su"
    )

    private fun findSu(): String? {
        for (p in SU_PATHS) {
            if (p == "su") return p
            if (File(p).exists()) return p
        }
        return null
    }

    fun isRooted(): Boolean {
        val su = findSu() ?: return false
        return try {
            val p = Runtime.getRuntime().exec(arrayOf(su, "-c", "id"))
            val out = BufferedReader(InputStreamReader(p.inputStream)).readText()
            p.waitFor()
            out.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }

    fun requestRoot(): Boolean {
        val su = findSu() ?: return false
        return try {
            val p = Runtime.getRuntime().exec(arrayOf(su, "-c", "id"))
            val out = BufferedReader(InputStreamReader(p.inputStream)).readText()
            p.waitFor()
            out.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }

    private fun runRoot(cmd: String): Boolean {
        val su = findSu() ?: return false
        return try {
            val p = Runtime.getRuntime().exec(arrayOf(su, "-c", cmd))
            p.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun blockIp(ip: String): Boolean =
        runRoot("iptables -I FORWARD -s $ip -j DROP; iptables -I FORWARD -d $ip -j DROP; iptables -I INPUT -s $ip -j DROP")

    fun unblockIp(ip: String): Boolean =
        runRoot("iptables -D FORWARD -s $ip -j DROP; iptables -D FORWARD -d $ip -j DROP; iptables -D INPUT -s $ip -j DROP")

    fun blockMac(mac: String): Boolean =
        if (mac.contains("?")) false
        else runRoot("iptables -I FORWARD -m mac --mac-source $mac -j DROP; iptables -I INPUT -m mac --mac-source $mac -j DROP")

    fun unblockMac(mac: String): Boolean =
        if (mac.contains("?")) false
        else runRoot("iptables -D FORWARD -m mac --mac-source $mac -j DROP; iptables -D INPUT -m mac --mac-source $mac -j DROP")
}
