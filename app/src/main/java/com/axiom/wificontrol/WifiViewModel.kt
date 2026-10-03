package com.axiom.wificontrol

import android.app.Application
import android.content.Context
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

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        _state.value = _state.value.copy(
            rooted = RootShell.isRooted(),
            netInfo = scanner.getNetInfo()
        )
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
            _state.value = _state.value.copy(
                scanning = true, progress = 0f,
                devices = emptyList(), message = null
            )
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
            _state.value = _state.value.copy(
                scanning = false,
                devices = result.map {
                    val k = if (it.mac.contains("?")) it.ip else it.mac.lowercase()
                    it.copy(
                        trusted = t.contains(k),
                        blocked = b.contains(k)
                    )
                },
                message = "Ketemu " + result.size + " device."
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
            val ok = if (nowBlocked) {
                RootShell.blockIp(dev.ip)
            } else {
                RootShell.unblockIp(dev.ip)
            }
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

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
