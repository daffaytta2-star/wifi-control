package com.axiom.wificontrol

import java.io.DataOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader

object RootShell {
    fun isRooted(): Boolean {
        val paths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/debug_ramdisk/su", "/data/adb/ksu/bin/su"
        )
        if (paths.any { java.io.File(it).exists() }) return true
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("which", "su"))
            p.waitFor() == 0 && p.inputStream.bufferedReader().readText().isNotBlank()
        } catch (_: Exception) { false }
    }

    fun requestRoot(): Boolean = try {
        val p = Runtime.getRuntime().exec("su")
        val out = DataOutputStream(p.outputStream)
        out.writeBytes("id\n")
        out.writeBytes("exit\n")
        out.flush()
        val r = BufferedReader(InputStreamReader(p.inputStream)).readText()
        p.waitFor()
        r.contains("uid=0")
    } catch (_: Exception) { false }

    fun blockIp(ip: String): Boolean = runRoot(
        "iptables -I FORWARD -s " + ip + " -j DROP\n" +
        "iptables -I FORWARD -d " + ip + " -j DROP\n" +
        "iptables -I INPUT  -s " + ip + " -j DROP\n"
    )

    fun unblockIp(ip: String): Boolean = runRoot(
        "iptables -D FORWARD -s " + ip + " -j DROP\n" +
        "iptables -D FORWARD -d " + ip + " -j DROP\n" +
        "iptables -D INPUT  -s " + ip + " -j DROP\n"
    )

    fun blockMac(mac: String): Boolean = runRoot(
        "iptables -I FORWARD -m mac --mac-source " + mac + " -j DROP\n" +
        "iptables -I INPUT  -m mac --mac-source " + mac + " -j DROP\n"
    )

    fun unblockMac(mac: String): Boolean = runRoot(
        "iptables -D FORWARD -m mac --mac-source " + mac + " -j DROP\n" +
        "iptables -D INPUT  -m mac --mac-source " + mac + " -j DROP\n"
    )

    private fun runRoot(cmds: String): Boolean = try {
        val p = Runtime.getRuntime().exec("su")
        val out = DataOutputStream(p.outputStream)
        out.writeBytes(cmds)
        out.writeBytes("exit\n")
        out.flush()
        p.waitFor() == 0
    } catch (_: Exception) { false }
}
