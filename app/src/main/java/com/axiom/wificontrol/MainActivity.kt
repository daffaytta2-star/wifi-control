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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

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
            AppTheme {
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
    val scope = rememberCoroutineScope()
    var pillWidth by remember { mutableIntStateOf(AppSettings.getPillWidth(ctx)) }
    var pillHeight by remember { mutableIntStateOf(AppSettings.getPillHeight(ctx)) }
    var pillFont by remember { mutableIntStateOf(AppSettings.getPillFont(ctx)) }
    val pkgVersion = remember { try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?" } catch (_: Exception) { "?" } }

    LaunchedEffect(state.message) {
        state.message?.let {
            snack.showSnackbar(it)
            vm.clearMessage()
        }
    }

    // Load history tiap pindah ke tab History
    LaunchedEffect(tab) {
        if (tab == 1) {
            scope.launch { history = vm.loadHistory() }
        }
    }

    // Auto-refresh scan tiap 30 detik
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000L)
            if (!state.scanning && tab == 0) {
                vm.scan()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Control v" + (ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?")) },
                actions = {
                    IconButton(
                        onClick = {
                            if (tab == 0) vm.scan()
                            else scope.launch { history = vm.loadHistory() }
                        },
                        enabled = !state.scanning
                    ) {
                        Icon(Icons.Filled.Refresh, "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp, vertical = 20.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .glassPill()
                        .padding(horizontal = 4.dp, vertical = pillHeight.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabPill(
                        modifier = Modifier.weight(1f),
                        selected = tab == 0,
                        icon = { Icon(Icons.Filled.CheckCircle, "Devices") },
                        label = "Devices",
                        onClick = { tab = 0 },
                        fontSize = pillFont,
                        verticalPadding = pillHeight
                    )
                    TabPill(
                        modifier = Modifier.weight(1f),
                        selected = tab == 1,
                        icon = { Icon(Icons.Filled.Refresh, "History") },
                        label = "History",
                        onClick = { tab = 1 },
                        fontSize = pillFont,
                        verticalPadding = pillHeight
                    )
                    TabPill(
                        modifier = Modifier.weight(1f),
                        selected = tab == 2,
                        icon = { Icon(Icons.Filled.Refresh, "Speed") },
                        label = "Speed",
                        onClick = { tab = 2 },
                        fontSize = pillFont,
                        verticalPadding = pillHeight
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().liquidBackground().padding(12.dp)) {

            if (tab != 2) {
                state.netInfo?.let { n ->
                    HeroCard(
                        netInfo = n,
                        rooted = state.rooted,
                        onOpenRouter = { RouterHelper.openRouter(ctx, n.gatewayIp) }
                    )
                }
            }

            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(tween(250)) + slideInHorizontally { it / 8 })
                        .togetherWith(fadeOut(tween(200)) + slideOutHorizontally { -it / 8 })
                },
                label = "tabTransition"
            ) { currentTab ->

            if (currentTab == 2) {
                SpeedTestTab(ctx)
            } else if (currentTab == 0) {
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
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("History (" + history.size + ")",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Button(onClick = {
                        scope.launch {
                            vm.clearHistory()
                            history = vm.loadHistory()
                        }
                    }) { Text("Hapus") }
                }
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
            }  // AnimatedContent
        }
    }
}

@Composable
fun DeviceRow(dev: Device, vm: WifiViewModel) {
    AppCard(
        modifier = Modifier.padding(vertical = AppSpacing.xs),
        padding = AppSpacing.md
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VendorAvatar(dev.vendor, size = 42.dp)
            Spacer(Modifier.width(AppSpacing.md))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dev.ip,
                        style = AppText.bodyBold,
                        color = AppColor.TextPrimary
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                    if (dev.blocked) {
                        StatusBadge("BLOCKED", AppColor.Danger, AppColor.DangerBg)
                    } else if (dev.trusted) {
                        StatusBadge("TRUSTED", AppColor.Success, AppColor.SuccessBg)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    dev.vendor + "  •  " + dev.mac,
                    style = AppText.caption,
                    color = AppColor.TextSecondary
                )
            }
            IconButton(onClick = { vm.toggleTrusted(dev) }) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Trust",
                    tint = if (dev.trusted) AppColor.Success else AppColor.TextMuted
                )
            }
            IconButton(onClick = { vm.toggleBlock(dev) }) {
                Icon(
                    Icons.Filled.Block,
                    contentDescription = "Block",
                    tint = if (dev.blocked) AppColor.Danger else AppColor.TextMuted
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

    Box(Modifier.fillMaxWidth().padding(vertical = 3.dp).glass()) {
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

@Composable
fun TabPill(
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    fontSize: Int = 10,
    verticalPadding: Int = 6,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) DarkGlassColors.PillActive else Color.Transparent
            )
            .then(
                if (selected) Modifier.border(
                    BorderStroke(1.dp, Color(0x55FFFFFF)),
                    RoundedCornerShape(22.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = verticalPadding.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = fontSize.sp,
                color = if (selected) Color.White else DarkGlassColors.TextSecondary,
                maxLines = 1
            )
        }
    }
}





@Composable
fun SettingsTab(
    ctx: android.content.Context,
    pillWidth: Int,
    pillHeight: Int,
    pillFont: Int,
    onPillWidthChange: (Int) -> Unit,
    onPillHeightChange: (Int) -> Unit,
    onPillFontChange: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(4.dp)) {

        Text("Pengaturan Pill Nav",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp))

        Text("Lebar Pill: " + pillWidth + "%", fontSize = 14.sp)
        Slider(
            value = pillWidth.toFloat(),
            onValueChange = {
                onPillWidthChange(it.toInt())
                AppSettings.setPillWidth(ctx, it.toInt())
            },
            valueRange = 60f..100f,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        Text("Tinggi Pill: " + pillHeight + "dp", fontSize = 14.sp)
        Slider(
            value = pillHeight.toFloat(),
            onValueChange = {
                onPillHeightChange(it.toInt())
                AppSettings.setPillHeight(ctx, it.toInt())
            },
            valueRange = 4f..20f,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        Text("Ukuran Font: " + pillFont + "sp", fontSize = 14.sp)
        Slider(
            value = pillFont.toFloat(),
            onValueChange = {
                onPillFontChange(it.toInt())
                AppSettings.setPillFont(ctx, it.toInt())
            },
            valueRange = 10f..16f,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text("Atur ukuran pill nav biar pas di layar lu",
            fontSize = 12.sp,
            color = DarkGlassColors.TextSecondary)
    }
}


@Composable
fun SpeedRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
