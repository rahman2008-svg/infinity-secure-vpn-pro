package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.vpn.ServerInfo
import com.example.vpn.ServerList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListSheet(
    activeServerCode: String,
    isWarpPlus: Boolean,
    onServerSelected: (ServerInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = All, 1 = Free, 2 = Premium (WARP+)

    val filteredServers = remember(searchQuery, selectedTab) {
        ServerList.servers.filter { server ->
            // Search filter
            val matchesSearch = server.country.contains(searchQuery, ignoreCase = true) ||
                    server.city.contains(searchQuery, ignoreCase = true)
            
            // Tab filter
            val matchesTab = when (selectedTab) {
                1 -> !server.isPremium // Free only
                2 -> server.isPremium  // Premium only
                else -> true           // All
            }

            matchesSearch && matchesTab
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkVoid)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Global Server Network",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Choose from 50+ secure endpoint locations",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close server selection",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Country or City...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    } else null,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DeepSlate,
                        unfocusedContainerColor = DeepSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tabs (All, Free, Premium)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberCyan
                        )
                    },
                    divider = { HorizontalDivider(color = DeepSlate) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("All Network", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Standard", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Premium (WARP+)", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Server List
                if (filteredServers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillGridHeight()
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌎", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No servers found matching query", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredServers, key = { it.code }) { server ->
                            ServerRow(
                                server = server,
                                isActive = activeServerCode == server.code,
                                isLocked = server.isPremium && !isWarpPlus,
                                onClick = {
                                    onServerSelected(server)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerRow(
    server: ServerInfo,
    isActive: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    // Dynamic load color representation
    val loadColor = when {
        server.baseLoad < 40 -> Color(0xFF00E676)  // Green
        server.baseLoad < 70 -> Color(0xFFFFD600)  // Yellow
        else -> Color(0xFFFF5252)                 // Red
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DeepSlate.copy(alpha = 0.9f) else DeepSlate
        ),
        border = if (isActive) BorderStroke(1.5.dp, CyberCyan) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Flag Emoji / Icon
                Text(
                    text = server.flag,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = server.country,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (server.isPremium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(PremiumOrange, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "WARP+",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = server.city,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Right Info: ping, load progress, and lock status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ping latency
                    Text(
                        text = "${server.basePing} ms",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (server.basePing < 30) Color(0xFF00E676) else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Connection load indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(loadColor)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                // Load percentage text
                Text(
                    text = "Load: ${server.baseLoad}%",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// Custom Grid size constraint
fun Modifier.fillGridHeight(): Modifier = this.heightIn(min = 200.dp)
