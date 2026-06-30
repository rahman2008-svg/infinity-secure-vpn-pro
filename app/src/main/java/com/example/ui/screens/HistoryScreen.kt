package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ConnectionLog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    logs: List<ConnectionLog>,
    onBack: () -> Unit,
    onClearAll: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalConnections = logs.size
    val totalBytes = logs.sumOf { it.bytesUploaded + it.bytesDownloaded }
    val peakSpeed = if (logs.isNotEmpty()) logs.maxOf { it.peakSpeed } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection History", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear all logs", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkVoid)
            )
        },
        containerColor = DarkVoid
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Stats Overview Cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                StatSumCard(
                    title = "Total Sessions",
                    value = totalConnections.toString(),
                    icon = "🔌",
                    modifier = Modifier.weight(1f)
                )
                StatSumCard(
                    title = "Secured Data",
                    value = formatBytes(totalBytes),
                    icon = "🛡️",
                    modifier = Modifier.weight(1.2f)
                )
                StatSumCard(
                    title = "Peak Tunnel",
                    value = String.format("%.1f KB/s", peakSpeed),
                    icon = "🚀",
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Recent Connection Logs",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No connection sessions logged yet.",
                            fontSize = 15.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your secure data tunnels are entirely client-side. Logs will show up here after you establish and disconnect a secure connection.",
                            fontSize = 11.sp,
                            color = TextSecondary.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(logs) { log ->
                        ConnectionLogItem(log = log)
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Clear All Sessions?") },
                text = { Text("Are you sure you want to permanently clear your secure session history? This action is irreversible.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClearAll()
                            showDeleteConfirm = false
                        }
                    ) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = DeepSlate,
                titleContentColor = Color.White,
                textContentColor = TextSecondary
            )
        }
    }
}

@Composable
fun StatSumCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = icon, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(CyberCyan, shape = CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ConnectionLogItem(log: ConnectionLog) {
    val durationMs = log.endTime - log.startTime
    val formattedDuration = formatDuration(durationMs)
    val formattedDate = remember(log.startTime) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(log.startTime))
    }

    // Flag emoji mapping for visual flair
    val flag = when (log.serverCode) {
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
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = flag, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                    Column {
                        Text(
                            text = log.serverCountry,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Mode Tag
                Box(
                    modifier = Modifier
                        .background(
                            if (log.connectionMode == "WARP_PLUS") PremiumOrange.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (log.connectionMode == "WARP_PLUS") "WARP+" else log.connectionMode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.connectionMode == "WARP_PLUS") PremiumOrange else CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DarkVoid.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LogDetailItem(label = "Duration", value = formattedDuration)
                LogDetailItem(label = "Uploaded", value = formatBytes(log.bytesUploaded))
                LogDetailItem(label = "Downloaded", value = formatBytes(log.bytesDownloaded))
                LogDetailItem(label = "Avg Speed", value = String.format("%.1f KB/s", log.peakSpeed * 0.45)) // estimate avg speed from peak
            }
        }
    }
}

@Composable
fun LogDetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// Global Formatter helpers
fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60)) % 24
    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}
