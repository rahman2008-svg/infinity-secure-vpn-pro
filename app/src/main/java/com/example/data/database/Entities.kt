package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey val id: Int = 1,
    val isOnboarded: Boolean = false,
    val autoConnect: Boolean = false,
    val alwaysOnVpn: Boolean = false,
    val dnsMode: String = "1.1.1.1", // "1.1.1.1", "1.1.1.2" (Malware), "1.1.1.3" (Family)
    val encryptionProtocol: String = "HTTPS", // "HTTPS" (DoH), "TLS" (DoT), "UDP" (Standard)
    val selectedServerCountry: String = "Auto Detect",
    val selectedServerCode: String = "AUTO",
    val selectedMode: String = "WARP" // "DNS", "WARP", "WARP_PLUS"
)

@Entity(tableName = "connection_logs")
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverCountry: String,
    val serverCode: String,
    val startTime: Long,
    val endTime: Long,
    val bytesUploaded: Long,
    val bytesDownloaded: Long,
    val peakSpeed: Double, // in KB/s
    val dnsMode: String,
    val connectionMode: String
)

@Entity(tableName = "trusted_networks")
data class TrustedNetwork(
    @PrimaryKey val ssid: String,
    val addedTime: Long = System.currentTimeMillis()
)
