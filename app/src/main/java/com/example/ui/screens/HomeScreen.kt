package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.vpn.ServerInfo
import com.example.vpn.VpnState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    vpnState: VpnState,
    bytesUploaded: Long,
    bytesDownloaded: Long,
    uploadSpeed: Double,
    downloadSpeed: Double,
    currentPing: Int,
    activeServer: String,
    activeServerCode: String,
    selectedMode: String,
    dnsMode: String,
    onToggleVpn: () -> Unit,
    onModeSelected: (String) -> Unit,
    onServerSelected: (ServerInfo) -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHistory: () -> Unit,
    onDnsModeSelected: (String) -> Unit = {}
) {
    var showServerSheet by remember { mutableStateOf(false) }
    var connectionDuration by remember { mutableStateOf("00:00") }
    var selectedBottomTab by remember { mutableStateOf(0) } // 0 = Secure, 1 = Family, 2 = Stats, 3 = Account

    // Real-time speed history for Stats tab
    val uploadHistory = remember { mutableStateListOf<Double>() }
    val downloadHistory = remember { mutableStateListOf<Double>() }

    // Pulsing loop for status and connections
    val infiniteTransition = rememberInfiniteTransition(label = "PulseGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Calculate connection duration reactively and update chart history
    LaunchedEffect(vpnState, uploadSpeed, downloadSpeed) {
        if (vpnState is VpnState.Connected) {
            // Track speeds
            uploadHistory.add(uploadSpeed)
            downloadHistory.add(downloadSpeed)
            if (uploadHistory.size > 25) uploadHistory.removeAt(0)
            if (downloadHistory.size > 25) downloadHistory.removeAt(0)

            while (true) {
                val elapsed = System.currentTimeMillis() - vpnState.startTime
                connectionDuration = formatDuration(elapsed)
                delay(1000)
            }
        } else {
            connectionDuration = "00:00"
            // Set some baseline flat values
            if (uploadHistory.size < 25) {
                uploadHistory.clear()
                downloadHistory.clear()
                repeat(25) {
                    uploadHistory.add(0.0)
                    downloadHistory.add(0.0)
                }
            }
        }
    }

    val isConnected = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting

    // Sophisticated theme accent colors
    val themeAccentBlue = Color(0xFF3B82F6) // blue-500
    val activeColor = if (selectedMode == "WARP_PLUS") PremiumOrange else themeAccentBlue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Sophisticated ambient blur behind everything
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isConnected) {
                            listOf(activeColor.copy(alpha = 0.12f), Color.Transparent)
                        } else {
                            listOf(Color.White.copy(alpha = 0.015f), Color.Transparent)
                        },
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HEADER (Styled exactly like the "Sophisticated Dark" HTML)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Infinity Secure",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2563EB), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO+",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text(
                        text = "Cloudflare Edge Infrastructure",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateHistory,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.04f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Connection Logs",
                            tint = TextPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.04f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. MAIN SCROLLABLE CONTENT AREA BASED ON ACTIVE BOTTOM TAB
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedBottomTab) {
                    0 -> {
                        // TAB 0: SECURE / CONNECTION CONTROL SCREEN
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Connection State Label Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        color = if (isConnected) activeColor.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isConnected) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            color = if (isConnected) activeColor else if (isConnecting) PremiumOrange else TextSecondary,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        isConnected -> "PROTECTED"
                                        isConnecting -> "CONNECTING"
                                        else -> "UNSECURED"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) activeColor else if (isConnecting) PremiumOrange else TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // The Big Slider Track Toggle
                            val trackHeight = 160.dp
                            val knobSize = 80.dp
                            val padding = 8.dp
                            val maxOffset = trackHeight - knobSize - (padding * 2)

                            val targetOffset = if (isConnected) 0.dp else maxOffset
                            val animatedOffset by animateDpAsState(
                                targetValue = targetOffset,
                                animationSpec = spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "ToggleKnobOffset"
                            )

                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(trackHeight)
                                    .background(
                                        color = Color.White.copy(alpha = 0.03f),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .padding(padding)
                                    .clickable { onToggleVpn() }
                            ) {
                                // Glowing aura behind the knob when connected
                                if (isConnected) {
                                    Box(
                                        modifier = Modifier
                                            .size(knobSize)
                                            .offset(y = animatedOffset)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(activeColor.copy(alpha = pulseAlpha), Color.Transparent)
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                }

                                // Sliding Knob
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(knobSize)
                                        .offset(y = animatedOffset)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = if (isConnected) {
                                                    listOf(activeColor.copy(alpha = 0.85f), activeColor)
                                                } else {
                                                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                                                }
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isConnected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                ) {
                                    if (isConnecting) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else {
                                        Canvas(modifier = Modifier.size(28.dp)) {
                                            val strokeWidth = 3.dp.toPx()
                                            drawArc(
                                                color = Color.White,
                                                startAngle = 135f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                            drawLine(
                                                color = Color.White,
                                                start = Offset(x = size.width / 2, y = 0f),
                                                end = Offset(x = size.width / 2, y = size.height * 0.45f),
                                                strokeWidth = strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = if (isConnected) "Connected" else if (isConnecting) "Securing Tunnel..." else "Disconnected",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isConnected) {
                                    "IP: 104.28.12.154 • $activeServer"
                                } else {
                                    "Your internet is private"
                                },
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            if (isConnected) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .background(activeColor.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
                                        .border(1.dp, activeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Secure Duration: $connectionDuration",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeColor
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: FAMILY FILTER CONTROL
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "🛡️ Family Shield Filter",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Configure automatic safety rules and DNS filtering on the active tunnel.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                FamilyFilterCard(
                                    title = "Standard 1.1.1.1",
                                    desc = "High-speed encrypted resolution. Protects routing queries but does not filter web content.",
                                    isActive = dnsMode == "1.1.1.1",
                                    badge = "DEFAULT",
                                    badgeColor = Color(0xFF3B82F6),
                                    onClick = { onDnsModeSelected("1.1.1.1") }
                                )

                                FamilyFilterCard(
                                    title = "Malware Protection 1.1.1.2",
                                    desc = "Automatically block known malicious domains, trojans, ransomware, and phishing systems.",
                                    isActive = dnsMode == "1.1.1.2",
                                    badge = "SECURITY",
                                    badgeColor = Color(0xFF10B981),
                                    onClick = { onDnsModeSelected("1.1.1.2") }
                                )

                                FamilyFilterCard(
                                    title = "Family Safeguard 1.1.1.3",
                                    desc = "Blocks malicious domains and adult content. Recommended for environments with children.",
                                    isActive = dnsMode == "1.1.1.3",
                                    badge = "KIDS SAFE",
                                    badgeColor = Color(0xFFF59E0B),
                                    onClick = { onDnsModeSelected("1.1.1.3") }
                                )
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: REAL-TIME STATISTICS DASHBOARD
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "📊 Real-Time Telemetry",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Active traffic analytics plotted on Cloudflare edge hops.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Real-time custom Canvas Speed Chart
                            RealTimeSpeedChart(
                                uploadHistory = uploadHistory,
                                downloadHistory = downloadHistory,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Traffic metrics grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatMetricItem(
                                    label = "Download",
                                    value = if (isConnected) String.format("%.1f KB/s", downloadSpeed) else "0.0 KB/s",
                                    indicatorColor = CyberCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricItem(
                                    label = "Upload",
                                    value = if (isConnected) String.format("%.1f KB/s", uploadSpeed) else "0.0 KB/s",
                                    indicatorColor = PremiumOrange,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatMetricItem(
                                    label = "Total Downloaded",
                                    value = formatBytes(bytesDownloaded),
                                    indicatorColor = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricItem(
                                    label = "Total Uploaded",
                                    value = formatBytes(bytesUploaded),
                                    indicatorColor = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: ACCOUNT & SETTINGS DETAIL
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "👤 Profile & Subscriptions",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Cloudflare Edge Smart Routing client credentials.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            PremiumProfileCard()
                        }
                    }
                }
            }

            // 3. CHOOSE LOCATION QUICK ACTION CARD (Only visible on Secure connection tab)
            if (selectedBottomTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepSlate.copy(alpha = 0.8f), shape = RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(24.dp))
                        .clickable { showServerSheet = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.04f), shape = CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (activeServerCode) {
                                        "AUTO" -> "⚡"
                                        "US_NY" -> "🇺🇸"
                                        "US_SF" -> "🇺🇸"
                                        "GB" -> "🇬🇧"
                                        "SG" -> "🇸🇬"
                                        "BD" -> "🇧🇩"
                                        "IN" -> "🇮🇳"
                                        "IN_BLR" -> "🇮🇳"
                                        "DE" -> "🇩🇪"
                                        "JP" -> "🇯🇵"
                                        "AU" -> "🇦🇺"
                                        "CA" -> "🇨🇦"
                                        "FR" -> "🇫🇷"
                                        "AE" -> "🇦🇪"
                                        "SA" -> "🇸🇦"
                                        "BR" -> "🇧🇷"
                                        "ZA" -> "🇿🇦"
                                        "NL" -> "🇳🇱"
                                        "CH" -> "🇨🇭"
                                        "KR" -> "🇰🇷"
                                        "MY" -> "🇲🇾"
                                        "TH" -> "🇹🇭"
                                        "ID" -> "🇮🇩"
                                        "VN" -> "🇻🇳"
                                        "PK" -> "🇵🇰"
                                        "IT" -> "🇮🇹"
                                        "ES" -> "🇪🇸"
                                        "SE" -> "🇸🇪"
                                        "NO" -> "🇳🇴"
                                        "FI" -> "🇫🇮"
                                        "HK" -> "🇭🇰"
                                        "TW" -> "🇹🇼"
                                        "TR" -> "🇹🇷"
                                        "EG" -> "🇪🇬"
                                        "QA" -> "🇶🇦"
                                        "KW" -> "🇰🇼"
                                        "NZ" -> "🇳🇿"
                                        "MX" -> "🇲🇽"
                                        "AR" -> "🇦🇷"
                                        "CL" -> "🇨🇱"
                                        "CO" -> "🇨🇴"
                                        "IE" -> "🇮🇪"
                                        "BE" -> "🇧🇪"
                                        "AT" -> "🇦🇹"
                                        "PL" -> "🇵🇱"
                                        "UA" -> "🇺🇦"
                                        "CZ" -> "🇨🇿"
                                        "GR" -> "🇬🇷"
                                        "PT" -> "🇵🇹"
                                        "SG_PREM" -> "🇸🇬"
                                        "BD_CTG" -> "🇧🇩"
                                        "IS" -> "🇮🇸"
                                        "JP_OSA" -> "🇯🇵"
                                        else -> "🌎"
                                    },
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Select Location",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$activeServer (54 Available • ${currentPing}ms)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Select Location Arrow",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. TUNNEL MODES (DNS ONLY, WARP, WARP+) (Only visible on Secure connection tab)
            if (selectedBottomTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepSlate.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ModeSelectorButton(
                            label = "DNS Only",
                            desc = "Encrypt resolves",
                            isSelected = selectedMode == "DNS",
                            activeColor = activeColor,
                            onClick = { onModeSelected("DNS") },
                            modifier = Modifier.weight(1f)
                        )
                        ModeSelectorButton(
                            label = "WARP",
                            desc = "Secure tunnel",
                            isSelected = selectedMode == "WARP",
                            activeColor = activeColor,
                            onClick = { onModeSelected("WARP") },
                            modifier = Modifier.weight(1f)
                        )
                        ModeSelectorButton(
                            label = "WARP+",
                            desc = "Argo routing",
                            isSelected = selectedMode == "WARP_PLUS",
                            activeColor = PremiumOrange,
                            onClick = { onModeSelected("WARP_PLUS") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. PRO STATS BOTTOM TAB BAR (FULLY INTERACTIVE NOW!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepSlate.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(20.dp))
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    label = "Secure",
                    icon = "🛡️",
                    isActive = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 }
                )
                BottomBarItem(
                    label = "Family",
                    icon = "👨‍👩‍👦",
                    isActive = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 }
                )
                BottomBarItem(
                    label = "Stats",
                    icon = "📊",
                    isActive = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 }
                )
                BottomBarItem(
                    label = "Account",
                    icon = "👤",
                    isActive = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 }
                )
            }
        }

        // Active Sheet selection overlay
        if (showServerSheet) {
            ServerListSheet(
                activeServerCode = activeServerCode,
                isWarpPlus = selectedMode == "WARP_PLUS",
                onServerSelected = onServerSelected,
                onDismiss = { showServerSheet = false }
            )
        }
    }
}

@Composable
fun BottomBarItem(
    label: String,
    icon: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isActive) 1f else 0.45f)
            .clickable { onClick() }
            .padding(horizontal = 12.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) CyberCyan else Color.White
        )
    }
}

@Composable
fun ModeSelectorButton(
    label: String,
    desc: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) activeColor.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) activeColor else TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 9.sp,
                color = if (isSelected) activeColor.copy(alpha = 0.7f) else TextSecondary
            )
        }
    }
}

@Composable
fun FamilyFilterCard(
    title: String,
    desc: String,
    isActive: Boolean,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DeepSlate.copy(alpha = 0.9f) else DeepSlate
        ),
        border = if (isActive) BorderStroke(1.5.dp, CyberCyan) else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                            .border(0.5.dp, badgeColor.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isActive) CyberCyan else Color.White.copy(alpha = 0.03f))
                    .border(1.dp, if (isActive) CyberCyan else Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected Filter",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatMetricItem(
    label: String,
    value: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DeepSlate.copy(alpha = 0.7f), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(indicatorColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun PremiumProfileCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Argo Smart Router",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Cloudflare Edge Tier 1 Network",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2563EB).copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE PRO+",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(18.dp))

            ProfileDetailRow(label = "Connection License", value = "Enterprise Unlimited SLA")
            ProfileDetailRow(label = "Security Mode", value = "DNS over HTTPS + WARP Tunnel")
            ProfileDetailRow(label = "Proxy Cryptography", value = "ChaCha20-Poly1305 AEAD")
            ProfileDetailRow(label = "Client License ID", value = "cl_warp_981a8c_edge")
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun RealTimeSpeedChart(
    uploadHistory: List<Double>,
    downloadHistory: List<Double>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        val maxSpeed = (uploadHistory.maxOrNull() ?: 0.0)
            .coerceAtLeast(downloadHistory.maxOrNull() ?: 0.0)
            .coerceAtLeast(50.0) // fallback base

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gridLines = 4

            // Draw grid lines
            for (i in 0..gridLines) {
                val y = height * i / gridLines
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Upload Speed Path (Orange)
            if (uploadHistory.size > 1) {
                val path = Path()
                val stepX = width / (uploadHistory.size - 1)
                uploadHistory.forEachIndexed { index, speed ->
                    val x = index * stepX
                    val normalizedSpeed = (speed / maxSpeed).coerceIn(0.0, 1.0)
                    val y = height - (normalizedSpeed * height).toFloat()
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = PremiumOrange,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Fill under
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(PremiumOrange.copy(alpha = 0.1f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )
            }

            // Draw Download Speed Path (Cyan)
            if (downloadHistory.size > 1) {
                val path = Path()
                val stepX = width / (downloadHistory.size - 1)
                downloadHistory.forEachIndexed { index, speed ->
                    val x = index * stepX
                    val normalizedSpeed = (speed / maxSpeed).coerceIn(0.0, 1.0)
                    val y = height - (normalizedSpeed * height).toFloat()
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = CyberCyan,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Fill under
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.1f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = String.format("%.0f KB/s", maxSpeed),
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = "0 KB/s",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
