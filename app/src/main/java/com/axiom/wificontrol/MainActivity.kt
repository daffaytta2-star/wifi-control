package com.axiom.wificontrol

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val vm: WifiViewModel by viewModels()

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPerms()
        val monitor = DeviceMonitor(this)
        monitor.start()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Screen(vm)
            }
        }
    }

    private fun requestPerms() {
        val list = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.NEARBY_WIFI_DEVICES)
                != PackageManager.PERMISSION_GRANTED)
                list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (list.isNotEmpty()) permLauncher.launch(list.toTypedArray())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(vm: WifiViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    var tab by remember { mutableIntStateOf(0) }
    var history by remember { mutableStateOf<List<DeviceHistory>>(emptyList()) }
    val ctx = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snack.showSnackbar(it)
            vm.clearMessage()
        }
    }

    // Load history tiap pindah ke tab History
    LaunchedEffect(tab) {
        if (tab == 1) {
            history = vm.loadHistory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Control") },
                actions = {
                    IconButton(
                        onClick = {
                            if (tab == 0) vm.scan()
                            else history = vm.loadHistory()
                        },
                        enabled = !state.scanning
                    ) {
                        Icon(Icons.Filled.Refresh, "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.CheckCircle, "Devices") },
                    label = { Text("Devices") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Refresh, "History") },
                    label = { Text("History") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {

            state.netInfo?.let { n ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("IP kamu: " + n.myIp, fontSize = 14.sp)
                        Text("Gateway: " + n.gatewayIp, fontSize = 14.sp)
                        Text("Subnet : " + n.subnet, fontSize = 14.sp)
                        Text("Root   : " + if (state.rooted) "YA" else "TIDAK",
                            fontSize = 14.sp,
                            color = if (state.rooted) Color(0xFF4CAF50) else Color(0xFFF44336))
                        Spacer(Modifier.height(8.dp))
                        val ctxBtn = LocalContext.current
                        Button(onClick = { RouterHelper.openRouter(ctxBtn, n.gatewayIp) }) {
                            Text("Buka Admin Router")
                        }
                    }
                }
            }

            if (tab == 0) {
                if (state.scanning) {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
                Text("Device (" + state.devices.size + ")",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp))
                LazyColumn(Modifier.weight(1f)) {
                    items(state.devices, key = { it.mac + it.ip }) { dev ->
                        DeviceRow(dev, vm)
                    }
                }
            } else {
                Text("History (" + history.size + ")",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp))
                if (history.isEmpty()) {
                    Text("Belom ada history. Scan dulu.",
                        fontSize = 13.sp, color = Color.Gray)
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(history, key = { it.id }) { h ->
                            HistoryRow(h)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceRow(dev: Device, vm: WifiViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(dev.ip, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(dev.mac, fontSize = 12.sp, color = Color.Gray)
                Text(dev.vendor + "  |  " + dev.hostname,
                    fontSize = 12.sp, color = Color.Gray)
                if (dev.blocked) {
                    Text("BLOCKED", fontSize = 11.sp, color = Color(0xFFF44336))
                } else if (dev.trusted) {
                    Text("TRUSTED", fontSize = 11.sp, color = Color(0xFF4CAF50))
                }
            }
            IconButton(onClick = { vm.toggleTrusted(dev) }) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Trust",
                    tint = if (dev.trusted) Color(0xFF4CAF50) else Color.Gray
                )
            }
            IconButton(onClick = { vm.toggleBlock(dev) }) {
                Icon(
                    Icons.Filled.Block,
                    contentDescription = "Block",
                    tint = if (dev.blocked) Color(0xFFF44336) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun HistoryRow(h: DeviceHistory) {
    val isConnect = h.event == "CONNECT"
    val color = if (isConnect) Color(0xFF4CAF50) else Color(0xFFF44336)
    val time = java.text.SimpleDateFormat("dd MMM HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(h.timestamp))

    Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(h.event, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                Text(h.ip + "  |  " + h.vendor, fontSize = 14.sp)
                Text(h.mac, fontSize = 11.sp, color = Color.Gray)
            }
            Text(time, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
