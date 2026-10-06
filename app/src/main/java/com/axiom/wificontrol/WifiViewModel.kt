package com.axiom.wificontrol

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val scanning: Boolean = false,
    val progress: Float = 0f,
    val devices: List<Device> = emptyList(),
    val rooted: Boolean = false,
    val netInfo: NetworkScanner.NetInfo? = null,
    val message: String? = null
)

class WifiViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("wifi_ctrl", Context.MODE_PRIVATE)
    private val scanner = NetworkScanner(app)
    private val db = AppDatabase.get(app)
    private val historyDao = db.historyDao()

    private val _state = MutableStateFlow(UiState())

    private fun logToFile(msg: String) {
        try {
            val dir = getApplication<Application>().getExternalFilesDir(null) ?: java.io.File("/data/local/tmp")
            if (!dir.exists()) dir.mkdirs()
            val f = java.io.File(dir, "wifi_control.log")
            f.appendText(java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date()) + " " + msg + "\n")
        } catch (_: Exception) {}
    }
    val state: StateFlow<UiState> = _state

    init {
        _state.value = _state.value.copy(
            rooted = RootShell.isRooted(),
            netInfo = scanner.getNetInfo()
        )
        // Tes DB pas init
        viewModelScope.launch {
            try {
                val c = historyDao.getAll()
                logToFile("DB OK count=" + c.size); Log.d("WiFiControl", "DB OK, count=" + c.size)
            } catch (e: Exception) {
                logToFile("DB ERR: " + e.message); Log.e("WiFiControl", "DB ERR: " + e.message, e)
                _state.value = _state.value.copy(message = "DB ERR: " + e.message)
            }
        }
    }

    private fun trustedSet() = prefs.getStringSet("trusted", emptySet()) ?: emptySet()
    private fun blockedSet() = prefs.getStringSet("blocked", emptySet()) ?: emptySet()

    private fun keyOf(d: Device): String =
        if (d.mac.contains("?")) d.ip else d.mac.lowercase()

    private fun saveLists(devices: List<Device>) {
        val trusted = devices.filter { it.trusted }.map { keyOf(it) }.toSet()
        val blocked = devices.filter { it.blocked }.map { keyOf(it) }.toSet()
        prefs.edit()
            .putStringSet("trusted", trusted)
            .putStringSet("blocked", blocked)
            .apply()
    }

    fun scan() {
        if (_state.value.scanning) return
        viewModelScope.launch {
            val oldKeys = _state.value.devices.map { keyOf(it) }.toSet()
            _state.value = _state.value.copy(
                scanning = true, progress = 0f,
                devices = emptyList(), message = null
            )
            val scanStart = System.currentTimeMillis()
            val result = scanner.scan(
                onProgress = { d, t ->
                    _state.value = _state.value.copy(
                        progress = if (t > 0) d.toFloat() / t else 0f
                    )
                },
                onDevice = { dev ->
                    _state.value = _state.value.copy(
                        devices = (_state.value.devices + dev).sortedBy {
                            it.ip.substringAfterLast(".").toIntOrNull() ?: 0
                        }
                    )
                }
            )
            val t = trustedSet()
            val b = blockedSet()
                        val newDevs = result.map {
                val k = keyOf(it)
                it.copy(trusted = t.contains(k), blocked = b.contains(k))
            }
            val newKeys = newDevs.map { keyOf(it) }.toSet()

            var insertErr: String? = null
            newDevs.forEach { d ->
                if (!oldKeys.contains(keyOf(d))) {
                    try {
                        historyDao.insert(DeviceHistory(
                            ip = d.ip, mac = d.mac,
                            vendor = d.vendor, event = "CONNECT"
                        ))
                        logToFile("INSERT OK: " + d.ip); Log.d("WiFiControl", "INSERT OK: " + d.ip)
                    } catch (e: Exception) {
                        insertErr = e.message
                        logToFile("INSERT ERR: " + e.message); Log.e("WiFiControl", "INSERT ERR: " + e.message, e)
                    }
                }
            }
            _state.value.devices.forEach { d ->
                if (!newKeys.contains(keyOf(d))) {
                    try {
                        historyDao.insert(DeviceHistory(
                            ip = d.ip, mac = d.mac,
                            vendor = d.vendor, event = "DISCONNECT"
                        ))
                    } catch (_: Exception) {}
                }
            }

            // Delay minimal 800ms biar progress keliatan
            val elapsed = System.currentTimeMillis() - scanStart
            if (elapsed < 800) {
                kotlinx.coroutines.delay(800 - elapsed)
            }

            _state.value = _state.value.copy(
                scanning = false,
                devices = newDevs,
                message = if (insertErr != null) "Insert ERR: " + insertErr
                          else "Ketemu " + result.size + " device."
            )
        }
    }

    fun toggleTrusted(dev: Device) {
        val k = keyOf(dev)
        val updated = _state.value.devices.map {
            if (keyOf(it) == k) it.copy(trusted = !it.trusted) else it
        }
        _state.value = _state.value.copy(devices = updated)
        saveLists(updated)
    }

    fun toggleBlock(dev: Device) {
        viewModelScope.launch {
            if (!_state.value.rooted) {
                _state.value = _state.value.copy(
                    message = "Non-root: blokir lewat admin router. IP: " + dev.ip
                )
                return@launch
            }
            if (!RootShell.requestRoot()) {
                _state.value = _state.value.copy(message = "Root ditolak.")
                return@launch
            }
            val nowBlocked = !dev.blocked
            val ok = if (nowBlocked) RootShell.blockIp(dev.ip)
                     else RootShell.unblockIp(dev.ip)
            if (ok) {
                val k = keyOf(dev)
                val updated = _state.value.devices.map {
                    if (keyOf(it) == k) it.copy(blocked = nowBlocked) else it
                }
                _state.value = _state.value.copy(
                    devices = updated,
                    message = if (nowBlocked) "Diblokir: " + dev.ip
                              else "Dibuka: " + dev.ip
                )
                saveLists(updated)
            } else {
                _state.value = _state.value.copy(message = "iptables gagal.")
            }
        }
    }

    suspend fun loadHistory(): List<DeviceHistory> {
        return try {
            val h = historyDao.getAll()
            logToFile("loadHistory: " + h.size); Log.d("WiFiControl", "loadHistory: " + h.size)
            h
        } catch (e: Exception) {
            logToFile("loadHistory ERR: " + e.message); Log.e("WiFiControl", "loadHistory ERR: " + e.message, e)
            _state.value = _state.value.copy(message = "Load ERR: " + e.message)
            emptyList()
        }
    }

    suspend fun clearHistory() {
        try { historyDao.clearAll() } catch (_: Exception) {}
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
