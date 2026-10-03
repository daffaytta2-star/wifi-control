package com.axiom.wificontrol

data class Device(
    val ip: String,
    val mac: String,
    var hostname: String = "?",
    var vendor: String = "Unknown",
    var trusted: Boolean = false,
    var blocked: Boolean = false
)

object VendorDb {
    private val oui = mapOf(
        "00:1a:2b" to "Apple",     "00:1b:63" to "Apple",
        "3c:5a:b4" to "Google",    "f4:f5:d8" to "Google",
        "b8:27:eb" to "RaspberryPi","dc:a6:32" to "RaspberryPi",
        "00:50:56" to "VMware",    "08:00:27" to "VirtualBox",
        "ec:fa:bc" to "Espressif", "24:0a:c4" to "Espressif",
        "c8:2b:96" to "Xiaomi",    "64:09:80" to "Xiaomi",
        "f8:a4:5f" to "Xiaomi",    "50:8f:4c" to "Xiaomi",
        "00:e0:4c" to "Realtek",   "00:1e:64" to "Intel",
        "94:65:9c" to "Intel",     "5c:0a:5b" to "Samsung",
        "28:39:5e" to "Samsung",   "b0:be:76" to "TP-Link",
        "a4:2b:b0" to "TP-Link",   "c0:25:e9" to "TP-Link"
    )
    fun lookup(mac: String): String =
        oui[mac.lowercase().take(8)] ?: "Unknown"
}
