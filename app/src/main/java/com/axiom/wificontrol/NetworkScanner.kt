package com.axiom.wificontrol

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.NetworkInterface

class NetworkScanner(private val ctx: Context) {

    data class NetInfo(val iface: String, val subnet: String,
                       val gatewayIp: String, val myIp: String)

    fun getNetInfo(): NetInfo? {
        val wm = ctx.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        val dhcp = wm.dhcpInfo ?: return null
        val myIp = intToIp(dhcp.ipAddress)
        val gw   = intToIp(dhcp.gateway)
        val subnet = myIp.substringBeforeLast(".") + ".0/24"
        return NetInfo("wlan0", subnet, gw, myIp)
    }

    private fun intToIp(v: Int): String =
        (v and 0xff).toString() + "." +
        (v shr 8 and 0xff) + "." +
        (v shr 16 and 0xff) + "." +
        (v shr 24 and 0xff)

    /** Lookup vendor via API online — fallback kalo OUI offline gak ketemu. */
    private fun lookupVendorOnline(mac: String): String {
        return try {
            val url = java.net.URL("https://api.macvendors.com/" + mac)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().readText().trim()
            } else "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    private fun readArpViaRoot(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ip neigh"))
            val reader = p.inputStream.bufferedReader()
            val output = reader.readText()
            p.waitFor()
            val regex = Regex("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+dev\\s+\\S+\\s+lladdr\\s+([0-9a-f:]{17})")
            regex.findAll(output).forEach { m ->
                map[m.groupValues[1]] = m.groupValues[2].lowercase()
            }
        } catch (_: Exception) {}
        return map
    }

    private fun readArpTable(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            java.io.File("/proc/net/arp").forEachLine { line ->
                val parts = line.split(Regex("\\s+"))
                if (parts.size >= 4 && parts[0].contains(".")) {
                    val ip = parts[0]
                    val mac = parts[3]
                    if (mac.matches(Regex("([0-9a-f]{2}:){5}[0-9a-f]{2}"))) {
                        map[ip] = mac
                    }
                }
            }
        } catch (_: Exception) {}
        return map
    }

    private suspend fun ping(ip: String, timeoutMs: Int = 800): Boolean =
        withContext(Dispatchers.IO) {
            try {
                InetAddress.getByName(ip).isReachable(timeoutMs)
            } catch (_: Exception) { false }
        }

    suspend fun scan(
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onDevice: (Device) -> Unit = {}
    ): List<Device> {
        val info = getNetInfo() ?: return emptyList()
        val base = info.myIp.substringBeforeLast(".")
        val hosts = (1..254).map { base + "." + it }
        var arp = readArpTable()
        if (arp.isEmpty()) {
            arp = readArpViaRoot()
        }
        val found = mutableListOf<Device>()
        val lock = Any()
        var done = 0

        val jobs = hosts.map { ip ->
            CoroutineScope(Dispatchers.IO).async {
                val ok = ping(ip)
                synchronized(lock) { done++; onProgress(done, hosts.size) }
                if (ok) {
                    val mac = arp[ip] ?: "??:??:??:??:??:??"
                    val host = try {
                        InetAddress.getByName(ip).canonicalHostName
                            .substringBefore(".").takeIf { it != ip } ?: "?"
                    } catch (_: Exception) { "?" }
                    var vendor = if (mac != "??:??:??:??:??:??")
                        VendorDb.lookup(mac) else "Unknown"
                    if (vendor == "Unknown" && mac != "??:??:??:??:??:??") {
                        vendor = lookupVendorOnline(mac)
                    }
                    val dev = Device(
                        ip = ip,
                        mac = mac,
                        hostname = host,
                        vendor = vendor
                    )
                    synchronized(lock) { found.add(dev) }
                    onDevice(dev)
                }
            }
        }
        jobs.awaitAll()
        return found.sortedBy { it.ip.substringAfterLast(".").toIntOrNull() ?: 0 }
    }

    fun getMyMac(ifaceName: String = "wlan0"): String = try {
        NetworkInterface.getByName(ifaceName)
            .hardwareAddress
            .joinToString(":") { "00".format(it) }
    } catch (_: Exception) { "??:??:??:??:??:??" }
}
