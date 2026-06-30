package com.example.ui.screens

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TrustedNetwork
import com.example.ui.theme.*
import com.example.ui.viewmodel.DiagnosticsReport
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    autoConnect: Boolean,
    dnsMode: String,
    encryptionProtocol: String,
    trustedNetworks: List<TrustedNetwork>,
    diagnosticsReport: DiagnosticsReport?,
    isDiagnosing: Boolean,
    onBack: () -> Unit,
    onToggleAutoConnect: (Boolean) -> Unit,
    onDnsModeSelected: (String) -> Unit,
    onEncryptionSelected: (String) -> Unit,
    onAddTrustedNetwork: (String) -> Unit,
    onRemoveTrustedNetwork: (String) -> Unit,
    onRunDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    var newSsidInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics & Settings", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkVoid)
            )
        },
        containerColor = DarkVoid
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION: HANDSHAKE & TUNNEL DIAGNOSTICS
            item {
                SectionHeader("HANDSHAKE DIAGNOSTICS")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Client Handshake Handlers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Verify your local virtual tunnels, interface carrier details, DNS-over-HTTPS routing, and live ping speeds.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isDiagnosing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = CyberCyan, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Pinging Cloudflare Edge resolves...", color = CyberCyan, fontSize = 12.sp)
                                }
                            }
                        } else {
                            diagnosticsReport?.let { report ->
                                DiagnosticReportContent(report)
                            } ?: run {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                    .background(DarkVoid.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No diagnostic report generated yet. Tap the button below to analyze.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.clickable { onRunDiagnostics() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onRunDiagnostics,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text("Analyze Network Handshake", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // SECTION: CONNECTION PREFERENCES
            item {
                SectionHeader("CONNECTION PREFERENCES")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Connect", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Initiate secure DNS tunneling immediately on app startup.", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = autoConnect,
                                onCheckedChange = onToggleAutoConnect,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkVoid,
                                    checkedTrackColor = CyberCyan,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = DarkVoid
                                )
                            )
                        }

                        HorizontalDivider(color = DarkVoid.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 12.dp))

                        // Always On VPN Note
                        Text("Always-on VPN Protocol", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "To keep secure tunnels persistent across device restarts, please configure 'Always-on VPN' manually within Android's Settings > Network & Internet > VPN > Infinity Secure VPN Pro+.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // SECTION: DNS FILTERING MODES
            item {
                SectionHeader("DNS FILTERING OPTIONS")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Parental & Gateway Filters",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Route requests through custom pre-filtered Cloudflare servers to intercept threats.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 1.1.1.1 Standard
                        DnsModeOption(
                            title = "Standard 1.1.1.1",
                            desc = "Provides standard secure DNS routing without domain filtering.",
                            isActive = dnsMode == "1.1.1.1",
                            onClick = { onDnsModeSelected("1.1.1.1") }
                        )

                        HorizontalDivider(color = DarkVoid.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                        // 1.1.1.2 Malware Block
                        DnsModeOption(
                            title = "Block Malware (1.1.1.2)",
                            desc = "Automated defense. Automatically intercept known malware, botnets, and spyware.",
                            isActive = dnsMode == "1.1.1.2",
                            onClick = { onDnsModeSelected("1.1.1.2") }
                        )

                        HorizontalDivider(color = DarkVoid.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                        // 1.1.1.3 Malware + Adult content
                        DnsModeOption(
                            title = "Malware + Adult Filters (1.1.1.3)",
                            desc = "Complete family safety. Blocks malware threats and parental adult domains.",
                            isActive = dnsMode == "1.1.1.3",
                            onClick = { onDnsModeSelected("1.1.1.3") }
                        )
                    }
                }
            }

            // SECTION: ENCRYPTION TRANSPORT PROTOCOLS
            item {
                SectionHeader("SECURE TRANSPORT PROTOCOL")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Transport Encryption Mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Specify how DNS tunnels communicate with secure remote resolvers.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // DoH (HTTPS)
                        ProtocolOption(
                            title = "DNS over HTTPS (DoH)",
                            desc = "Embeds queries inside standard HTTPS traffic to bypass corporate firewall blocking.",
                            isActive = encryptionProtocol == "HTTPS",
                            onClick = { onEncryptionSelected("HTTPS") }
                        )

                        HorizontalDivider(color = DarkVoid.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                        // DoT (TLS)
                        ProtocolOption(
                            title = "DNS over TLS (DoT)",
                            desc = "Establishes a raw secure TLS tunnel dedicated solely to lightning fast DNS resolution.",
                            isActive = encryptionProtocol == "TLS",
                            onClick = { onEncryptionSelected("TLS") }
                        )

                        HorizontalDivider(color = DarkVoid.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                        // UDP (Standard)
                        ProtocolOption(
                            title = "Standard UDP Port 53",
                            desc = "Unencrypted, standard connection. Useful if TLS/HTTPS protocols are blocked on local Wi-Fi.",
                            isActive = encryptionProtocol == "UDP",
                            onClick = { onEncryptionSelected("UDP") }
                        )
                    }
                }
            }

            // SECTION: TRUSTED WI-FI NETWORKS
            item {
                SectionHeader("TRUSTED NETWORKS")
            }

            if (trustedNetworks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No Trusted Wi-Fi Networks Added", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Add Wi-Fi SSIDs where you trust the gateway, allowing VPN tunnels to bypass or auto-disconnect.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            
                            AddSsidControls(
                                value = newSsidInput,
                                onValueChange = { newSsidInput = it },
                                onAdd = {
                                    if (newSsidInput.isNotBlank()) {
                                        onAddTrustedNetwork(newSsidInput)
                                        newSsidInput = ""
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AddSsidControls(
                                value = newSsidInput,
                                onValueChange = { newSsidInput = it },
                                onAdd = {
                                    if (newSsidInput.isNotBlank()) {
                                        onAddTrustedNetwork(newSsidInput)
                                        newSsidInput = ""
                                    }
                                }
                            )
                        }
                    }
                }

                items(trustedNetworks, key = { it.ssid }) { network ->
                    TrustedNetworkRow(network = network, onDelete = { onRemoveTrustedNetwork(network.ssid) })
                }
            }

            // Spacer bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Infinity Secure VPN Pro+ • Version 1.0.0\nSecure client-side DNS & WARP tunnels",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = CyberCyan,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun DnsModeOption(
    title: String,
    desc: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isActive,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = CyberCyan,
                unselectedColor = TextSecondary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isActive) CyberCyan else Color.White)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
fun ProtocolOption(
    title: String,
    desc: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isActive,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = CyberCyan,
                unselectedColor = TextSecondary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isActive) CyberCyan else Color.White)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
fun DiagnosticReportContent(report: DiagnosticsReport) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkVoid.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text("Handshake Analysis", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 13.sp)
        
        DiagnosticDetailRow("Client IPv4 IP", report.localIp)
        DiagnosticDetailRow("Local Tunnel Gateway", report.gateway)
        DiagnosticDetailRow("Carrier Network Interface", report.interfaceLabel)
        DiagnosticDetailRow("VpnService Tunnel Interface", report.tunnelStatus)
        DiagnosticDetailRow("Active Encryption Protocol", report.dnsProtocol)
        DiagnosticDetailRow("Active Gateway Filters", when(report.dnsSecureMode) {
            "1.1.1.2" -> "Malware Intercept"
            "1.1.1.3" -> "Malware + Parental Safeguards"
            else -> "Standard 1.1.1.1 Secure"
        })
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cloudflare Resolver Ping", fontSize = 11.sp, color = TextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(if (report.isCloudflareReachable) Color(0xFF00E676) else Color.Red, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (report.isCloudflareReachable) "${report.latencyMs} ms (Optimal)" else "Offline/Blocked",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (report.isCloudflareReachable) Color(0xFF00E676) else Color.Red
                )
            }
        }
    }
}

@Composable
fun DiagnosticDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddSsidControls(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Enter Wi-Fi SSID Name", color = TextSecondary, fontSize = 13.sp) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkVoid,
                unfocusedContainerColor = DarkVoid,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = CyberCyan,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f).height(50.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(50.dp).minimumInteractiveComponentSize()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add SSID", tint = DarkVoid)
        }
    }
}

@Composable
fun TrustedNetworkRow(
    network: TrustedNetwork,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📶", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(text = network.ssid, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Secure tunnel bypass active", fontSize = 10.sp, color = TextSecondary)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete SSID", tint = Color.Red)
            }
        }
    }
}
