package com.drs.surifi.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drs.surifi.localsend.LocalPeer
import com.drs.surifi.localsend.LocalSendEngine
import com.drs.surifi.localsend.LocalSendState
import com.drs.surifi.netmerge.BondingTelemetry
import com.drs.surifi.netmerge.MultiPathBondingEngine
import kotlinx.coroutines.delay

enum class PortalDestination {
    SPLASH_S_LOGO,
    DUAL_CIRCLE_HUB,
    NETMERGE_PORTAL,
    LOCALSEND_PORTAL
}

@Composable
fun SurifiPortalApp(
    bondingEngine: MultiPathBondingEngine,
    localSendEngine: LocalSendEngine
) {
    val telemetry by bondingEngine.telemetry.collectAsState()
    val localSendState by localSendEngine.state.collectAsState()
    var currentPortal by remember { mutableStateOf(PortalDestination.SPLASH_S_LOGO) }

    LaunchedEffect(Unit) {
        bondingEngine.startBonding()
        localSendEngine.startLocalSendRadar()
        delay(1250L)
        currentPortal = PortalDestination.DUAL_CIRCLE_HUB
    }

    BackHandler(enabled = currentPortal != PortalDestination.DUAL_CIRCLE_HUB && currentPortal != PortalDestination.SPLASH_S_LOGO) {
        currentPortal = PortalDestination.DUAL_CIRCLE_HUB
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurifiPalette.ObsidianBg)
            .systemBarsPadding()
    ) {
        AnimatedContent(
            targetState = currentPortal,
            transitionSpec = {
                (fadeIn(tween(520, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.82f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)))
                    .togetherWith(fadeOut(tween(320)) + scaleOut(targetScale = 1.12f, animationSpec = tween(360)))
            },
            label = "LiquidGlassPortalTransition",
            modifier = Modifier.fillMaxSize().padding(bottom = 36.dp)
        ) { destination ->
            when (destination) {
                PortalDestination.SPLASH_S_LOGO -> MinimalistSSplashView()
                PortalDestination.DUAL_CIRCLE_HUB -> DualCircleHubScreen(
                    telemetry = telemetry,
                    localSendState = localSendState,
                    onOpenNetMerge = { currentPortal = PortalDestination.NETMERGE_PORTAL },
                    onOpenLocalSend = { currentPortal = PortalDestination.LOCALSEND_PORTAL }
                )
                PortalDestination.NETMERGE_PORTAL -> NetMergePortalScreen(
                    telemetry = telemetry,
                    onRunBenchmark = { bondingEngine.runDualPipeBenchmark() },
                    onToggleBonding = {
                        if (telemetry.isBondingActive) bondingEngine.stopBonding() else bondingEngine.startBonding()
                    },
                    onBackToHub = { currentPortal = PortalDestination.DUAL_CIRCLE_HUB },
                    onSwitchToLocalSend = { currentPortal = PortalDestination.LOCALSEND_PORTAL }
                )
                PortalDestination.LOCALSEND_PORTAL -> LocalSendPortalScreen(
                    state = localSendState,
                    onSendText = { peer, text -> localSendEngine.sendEncryptedText(peer, text) },
                    onSendFile = { peer, uri -> localSendEngine.sendEncryptedFile(peer, uri) },
                    onAddLoopbackPeer = { localSendEngine.addLoopbackOrManualPeer() },
                    onBackToHub = { currentPortal = PortalDestination.DUAL_CIRCLE_HUB },
                    onSwitchToNetMerge = { currentPortal = PortalDestination.NETMERGE_PORTAL }
                )
            }
        }

        Text(
            text = "Made by Dr. S ॐ",
            color = SurifiPalette.MutedSilver.copy(alpha = 0.55f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.8.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun MinimalistSSplashView() {
    val scaleAnim = remember { Animatable(0.86f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MinimalistSLogoCanvas(modifier = Modifier.size(116.dp).scale(scaleAnim.value))
            Spacer(modifier = Modifier.height(18.dp))
            Text("S U R I F I", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp, fontWeight = FontWeight.Light, letterSpacing = 6.sp)
        }
    }
}

@Composable
private fun DualCircleHubScreen(
    telemetry: BondingTelemetry,
    localSendState: LocalSendState,
    onOpenNetMerge: () -> Unit,
    onOpenLocalSend: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape).padding(4.dp)
            ) {
                MinimalistSLogoCanvas(modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("SURIFI", color = SurifiPalette.IceWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 4.5.sp)
        }

        LiquidGlassSphere(modifier = Modifier.size(240.dp), accentGlow = SurifiPalette.CyanGlow, onClick = onOpenNetMerge) {
            Column(
                modifier = Modifier.fillMaxSize().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("MULTI-PATH BONDING", color = SurifiPalette.MutedSilver, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.2.sp)
                Spacer(modifier = Modifier.height(6.dp))
                InfinityToOmCanvas(modifier = Modifier.size(108.dp), isBondingActive = telemetry.isBondingActive)
                Spacer(modifier = Modifier.height(6.dp))
                Text("${telemetry.combinedHarmonicMbps} Mbps", color = SurifiPalette.IceWhite, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace)
                Text("TAP TO ENTER NETMERGE", color = SurifiPalette.CyanGlow.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        LiquidGlassSphere(modifier = Modifier.size(240.dp), accentGlow = SurifiPalette.EmeraldPulse, onClick = onOpenLocalSend) {
            Column(
                modifier = Modifier.fillMaxSize().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("LOCALSEND P2P", color = SurifiPalette.MutedSilver, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LocalSendRadarCanvas(modifier = Modifier.size(104.dp), peerCount = localSendState.discoveredPeers.size)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${localSendState.discoveredPeers.size} Active Node(s)", color = SurifiPalette.IceWhite, fontSize = 18.sp, fontWeight = FontWeight.Light)
                Text("TAP TO SHARE FILES", color = SurifiPalette.EmeraldPulse.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun NetMergePortalScreen(
    telemetry: BondingTelemetry,
    onRunBenchmark: () -> Unit,
    onToggleBonding: () -> Unit,
    onBackToHub: () -> Unit,
    onSwitchToLocalSend: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PortalHeaderBar(
            title = "SURIFI • NETMERGE",
            subtitle = telemetry.statusMessage,
            onBackToHub = onBackToHub,
            switchLabel = "LocalSend",
            onSwitchPortal = onSwitchToLocalSend
        )

        LiquidGlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), glowColor = SurifiPalette.CyanGlow) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COMBINED HARMONIC THROUGHPUT", color = SurifiPalette.MutedSilver, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                InfinityToOmCanvas(modifier = Modifier.size(128.dp).padding(vertical = 6.dp), isBondingActive = telemetry.isBondingActive)
                Text("${telemetry.combinedHarmonicMbps}", color = SurifiPalette.IceWhite, fontSize = 44.sp, fontWeight = FontWeight.ExtraLight, fontFamily = FontFamily.Monospace)
                Text("Mbps • Dual-Socket Multiplexed", color = SurifiPalette.CyanGlow, fontSize = 12.sp, letterSpacing = 1.2.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BandwidthMeterCard(
                    modifier = Modifier.weight(1f),
                    label = "WI-FI LOOP",
                    value = "${telemetry.wifiLoopMbps} Mbps",
                    subValue = if (telemetry.wifiConnected) "RTT ${telemetry.wifiRttMs} ms • Bound" else "Awaiting Link",
                    accent = SurifiPalette.CyanGlow
                )
                BandwidthMeterCard(
                    modifier = Modifier.weight(1f),
                    label = "MOBILE LOOP",
                    value = "${telemetry.mobileLoopMbps} Mbps",
                    subValue = if (telemetry.cellularConnected) "RTT ${telemetry.cellularRttMs} ms • Bound" else "Awaiting 5G/LTE",
                    accent = SurifiPalette.SacredGold
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BandwidthMeterCard(
                    modifier = Modifier.weight(1f),
                    label = "PEAK DUAL-PIPE BENCHMARK",
                    value = "${telemetry.peakDualPipeMbps} Mbps",
                    subValue = "Zero-Dropout Lock",
                    accent = SurifiPalette.EmeraldPulse
                )
                BandwidthMeterCard(
                    modifier = Modifier.weight(1f),
                    label = "ANTI-STRAGGLER ENGINE",
                    value = "${telemetry.stragglerPacketsRescued} Rescued",
                    subValue = "${telemetry.failoverEventsHandled} Failovers Guarded",
                    accent = SurifiPalette.IceWhite
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidGlassCard(modifier = Modifier.weight(1.4f).height(56.dp), cornerRadius = 28.dp, glowColor = SurifiPalette.CyanGlow, onClick = onRunBenchmark) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = SurifiPalette.CyanGlow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (telemetry.isBenchmarking) "BENCHMARKING..." else "DUAL-PIPE SPEED TEST", color = SurifiPalette.IceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
                }
            }
            LiquidGlassCard(
                modifier = Modifier.weight(0.8f).height(56.dp),
                cornerRadius = 28.dp,
                glowColor = if (telemetry.isBondingActive) SurifiPalette.EmeraldPulse else SurifiPalette.MutedSilver,
                onClick = onToggleBonding
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (telemetry.isBondingActive) "BOND: ON" else "BOND: OFF", color = SurifiPalette.IceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp)
                }
            }
        }
    }
}

@Composable
private fun BandwidthMeterCard(modifier: Modifier = Modifier, label: String, value: String, subValue: String, accent: Color) {
    LiquidGlassCard(modifier = modifier, cornerRadius = 22.dp, glowColor = accent) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(label, color = SurifiPalette.MutedSilver, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = SurifiPalette.IceWhite, fontSize = 20.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subValue, color = accent.copy(alpha = 0.85f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun LocalSendPortalScreen(
    state: LocalSendState,
    onSendText: (LocalPeer, String) -> Unit,
    onSendFile: (LocalPeer, Uri) -> Unit,
    onAddLoopbackPeer: () -> Unit,
    onBackToHub: () -> Unit,
    onSwitchToNetMerge: () -> Unit
) {
    var selectedPeer by remember(state.discoveredPeers) { mutableStateOf(state.discoveredPeers.firstOrNull()) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val target = selectedPeer
        if (uri != null && target != null) onSendFile(target, uri)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PortalHeaderBar(
            title = "SURIFI • LOCALSEND",
            subtitle = "${state.deviceAlias} (${state.localIp}) • TLS ${state.securityFingerprint}",
            onBackToHub = onBackToHub,
            switchLabel = "NetMerge",
            onSwitchPortal = onSwitchToNetMerge
        )

        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), glowColor = SurifiPalette.EmeraldPulse) {
            Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                LocalSendRadarCanvas(modifier = Modifier.size(76.dp), peerCount = state.discoveredPeers.size)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.statusBanner, color = SurifiPalette.EmeraldPulse, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Multicast 224.0.0.167:53317 • AES-256-GCM", color = SurifiPalette.MutedSilver, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("+ Add Self/Loopback Node for Instant Test", color = SurifiPalette.IceWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onAddLoopbackPeer() })
                }
            }
        }

        Text("DISCOVERED SUBNET NODES", color = SurifiPalette.MutedSilver, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp)

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.discoveredPeers.isEmpty()) {
                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, glowColor = SurifiPalette.MutedSilver, onClick = onAddLoopbackPeer) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Listening for nearby SURIFI / LocalSend devices on Wi-Fi or Hotspot...", color = SurifiPalette.MutedSilver, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap here to spawn a Loopback Node to test E2E transfers now", color = SurifiPalette.EmeraldPulse, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                items(state.discoveredPeers, key = { it.id }) { peer ->
                    val isSelected = selectedPeer?.id == peer.id
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        glowColor = if (isSelected) SurifiPalette.EmeraldPulse else SurifiPalette.CyanGlow,
                        onClick = { selectedPeer = peer }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(peer.alias, color = SurifiPalette.IceWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("${peer.deviceModel} • ${peer.ipAddress}:${peer.port}", color = SurifiPalette.MutedSilver, fontSize = 11.sp)
                                Text("TLS/GCM: ${peer.tlsFingerprint}", color = SurifiPalette.EmeraldPulse.copy(alpha = 0.8f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { selectedPeer = peer; showNoteDialog = true }) {
                                    Icon(Icons.Rounded.EditNote, contentDescription = "Send Text", tint = SurifiPalette.IceWhite)
                                }
                                IconButton(onClick = { selectedPeer = peer; filePickerLauncher.launch(arrayOf("*/*")) }) {
                                    Icon(Icons.Rounded.UploadFile, contentDescription = "Send File", tint = SurifiPalette.EmeraldPulse)
                                }
                            }
                        }
                    }
                }
            }

            if (state.transfers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ENCRYPTED TRANSFER STREAM", color = SurifiPalette.MutedSilver, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                }
                items(state.transfers, key = { it.id }) { item ->
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, glowColor = SurifiPalette.CyanGlow) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${if (item.isIncoming) "↓ Received" else "↑ Sent"} • ${item.title}", color = SurifiPalette.IceWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(if (item.isCompleted) "100% VERIFIED" else "${(item.progress * 100).toInt()}%", color = SurifiPalette.EmeraldPulse, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (!item.previewText.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.previewText, color = SurifiPalette.MutedSilver, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog && selectedPeer != null) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            containerColor = Color(0xFF0E1118),
            titleContentColor = SurifiPalette.IceWhite,
            textContentColor = SurifiPalette.IceWhite,
            title = { Text("Send E2E Encrypted Text to ${selectedPeer?.alias}") },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Paste text, link, or key...", color = SurifiPalette.MutedSilver) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = selectedPeer
                    if (target != null && noteInput.isNotBlank()) {
                        onSendText(target, noteInput.trim())
                        noteInput = ""
                        showNoteDialog = false
                    }
                }) { Text("SEND E2E", color = SurifiPalette.EmeraldPulse) }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("CANCEL", color = SurifiPalette.MutedSilver) }
            }
        )
    }
}

@Composable
private fun PortalHeaderBar(title: String, subtitle: String, onBackToHub: () -> Unit, switchLabel: String, onSwitchPortal: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        LiquidGlassCard(modifier = Modifier.size(42.dp), cornerRadius = 21.dp, onClick = onBackToHub) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.GridView, contentDescription = "Dual Circle Hub", tint = SurifiPalette.IceWhite, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = SurifiPalette.IceWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.4.sp)
            Text(subtitle, color = SurifiPalette.MutedSilver, fontSize = 10.sp, maxLines = 1)
        }
        LiquidGlassCard(modifier = Modifier.height(38.dp), cornerRadius = 19.dp, onClick = onSwitchPortal) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = SurifiPalette.CyanGlow, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(switchLabel, color = SurifiPalette.IceWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
