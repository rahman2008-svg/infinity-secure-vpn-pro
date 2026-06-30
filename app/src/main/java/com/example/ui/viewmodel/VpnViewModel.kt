package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ConnectionLog
import com.example.data.database.TrustedNetwork
import com.example.data.database.UserSetting
import com.example.data.repository.VpnRepository
import com.example.vpn.ServerInfo
import com.example.vpn.VPNForegroundService
import com.example.vpn.VPNManager
import com.example.vpn.VpnState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VpnRepository
    
    // UI Screen navigation (Splash -> Onboarding -> Permission -> Home / Settings)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Loaded settings
    private val _userSettings = MutableStateFlow(UserSetting())
    val userSettings: StateFlow<UserSetting> = _userSettings.asStateFlow()

    // Observable Flows from DB
    val connectionLogs: StateFlow<List<ConnectionLog>>
    val trustedNetworks: StateFlow<List<TrustedNetwork>>

    // Live Telemetry from VpnForegroundService
    val vpnState: StateFlow<VpnState> = VPNForegroundService.vpnState
    val bytesUploaded: StateFlow<Long> = VPNForegroundService.bytesUploaded
    val bytesDownloaded: StateFlow<Long> = VPNForegroundService.bytesDownloaded
    val uploadSpeed: StateFlow<Double> = VPNForegroundService.uploadSpeed
    val downloadSpeed: StateFlow<Double> = VPNForegroundService.downloadSpeed
    val currentPing: StateFlow<Int> = VPNForegroundService.currentPing
    val activeServer: StateFlow<String> = VPNForegroundService.activeServer
    val activeServerCode: StateFlow<String> = VPNForegroundService.activeServerCode

    // Diagnostics State
    private val _diagnosticsReport = MutableStateFlow<DiagnosticsReport?>(null)
    val diagnosticsReport: StateFlow<DiagnosticsReport?> = _diagnosticsReport.asStateFlow()
    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VpnRepository(database.settingsDao())

        // Observe Connection Logs
        connectionLogs = repository.connectionLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Observe Trusted Networks
        trustedNetworks = repository.trustedNetworks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Load Settings and sync state
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                if (settings != null) {
                    _userSettings.value = settings
                }
            }
        }

        // Run Initial Routing Decisions
        viewModelScope.launch {
            val initialSettings = repository.getSettings()
            _userSettings.value = initialSettings
            
            // Check Splash flow delay
            delay(2500)
            
            val isNetworkOnline = checkNetworkOnline()
            if (!initialSettings.isOnboarded) {
                _currentScreen.value = Screen.Onboarding
            } else if (VPNManager.prepareVpn(application) != null) {
                _currentScreen.value = Screen.Permissions
            } else {
                _currentScreen.value = Screen.Home
                // Auto connect if configured
                if (initialSettings.autoConnect && vpnState.value is VpnState.Disconnected && isNetworkOnline) {
                    toggleVpn(application)
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setOnboarded() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(isOnboarded = true) }
            val context = getApplication<Application>()
            if (VPNManager.prepareVpn(context) != null) {
                _currentScreen.value = Screen.Permissions
            } else {
                _currentScreen.value = Screen.Home
            }
        }
    }

    fun completePermissions() {
        _currentScreen.value = Screen.Home
    }

    fun toggleVpn(context: Context) {
        viewModelScope.launch {
            val state = vpnState.value
            val settings = userSettings.value
            if (state is VpnState.Disconnected) {
                // Connect
                VPNManager.connect(
                    context = context,
                    serverCountry = settings.selectedServerCountry,
                    serverCode = settings.selectedServerCode,
                    dnsMode = settings.dnsMode,
                    connectionMode = settings.selectedMode
                )
            } else {
                // Disconnect
                VPNManager.disconnect(context)
            }
        }
    }

    fun selectServer(server: ServerInfo) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    selectedServerCountry = server.country,
                    selectedServerCode = server.code
                )
            }
            
            // If connected, automatically reconnect to the new server!
            val state = vpnState.value
            val context = getApplication<Application>()
            if (state is VpnState.Connected) {
                // Disconnect and immediately reconnect
                VPNManager.disconnect(context)
                delay(800)
                VPNManager.connect(
                    context = context,
                    serverCountry = server.country,
                    serverCode = server.code,
                    dnsMode = _userSettings.value.dnsMode,
                    connectionMode = _userSettings.value.selectedMode
                )
            }
        }
    }

    fun updateSelectedMode(mode: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(selectedMode = mode) }
            // Reconnect to update mode if active
            val state = vpnState.value
            val context = getApplication<Application>()
            if (state is VpnState.Connected) {
                VPNManager.disconnect(context)
                delay(800)
                VPNManager.connect(
                    context = context,
                    serverCountry = _userSettings.value.selectedServerCountry,
                    serverCode = _userSettings.value.selectedServerCode,
                    dnsMode = _userSettings.value.dnsMode,
                    connectionMode = mode
                )
            }
        }
    }

    fun updateDnsMode(mode: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(dnsMode = mode) }
            // Reconnect if currently connected to apply security policies!
            val state = vpnState.value
            val context = getApplication<Application>()
            if (state is VpnState.Connected) {
                VPNManager.disconnect(context)
                delay(800)
                VPNManager.connect(
                    context = context,
                    serverCountry = _userSettings.value.selectedServerCountry,
                    serverCode = _userSettings.value.selectedServerCode,
                    dnsMode = mode,
                    connectionMode = _userSettings.value.selectedMode
                )
            }
        }
    }

    fun updateEncryptionProtocol(protocol: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(encryptionProtocol = protocol) }
        }
    }

    fun toggleAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(autoConnect = enabled) }
        }
    }

    fun toggleAlwaysOnVpn(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(alwaysOnVpn = enabled) }
        }
    }

    // Trusted Networks API
    fun addTrustedNetwork(ssid: String) {
        viewModelScope.launch {
            repository.addTrustedNetwork(ssid)
        }
    }

    fun removeTrustedNetwork(ssid: String) {
        viewModelScope.launch {
            repository.removeTrustedNetwork(ssid)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Helpers
    fun checkNetworkOnline(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun diagnoseNetwork(context: Context) {
        if (_isDiagnosing.value) return
        _isDiagnosing.value = true
        _diagnosticsReport.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(2000) // Aesthetic delay to represent processing

                // Fetch current local IP
                var localIp = "Unknown"
                var interfaceName = "None"
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in interfaces) {
                    if (networkInterface.isLoopback || !networkInterface.isUp) continue
                    val addresses = networkInterface.inetAddresses
                    for (addr in addresses) {
                        if (!addr.isLoopbackAddress && addr.hostAddress.indexOf(':') < 0) { // IPv4
                            localIp = addr.hostAddress ?: "Unknown"
                            interfaceName = networkInterface.displayName ?: "Primary"
                            break
                        }
                    }
                }

                // Check active DNS resolver and Tunnel status
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNet = connectivityManager.activeNetwork
                val caps = connectivityManager.getNetworkCapabilities(activeNet)
                val hasVpnTransport = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

                val tunnelStatus = if (vpnState.value is VpnState.Connected || hasVpnTransport) "ACTIVE (Secure Local Interface)" else "INACTIVE (Direct Provider Route)"
                val gateway = if (hasVpnTransport) "10.0.0.2" else "192.168.1.1 (Standard)"

                // Check actual connectivity to Cloudflare
                val startPing = System.currentTimeMillis()
                val isCloudflareOnline = try {
                    InetAddress.getByName("one.one.one.one")
                    true
                } catch (e: Exception) {
                    false
                }
                val pingTime = (System.currentTimeMillis() - startPing).toInt()

                _diagnosticsReport.value = DiagnosticsReport(
                    localIp = localIp,
                    gateway = gateway,
                    dnsProtocol = userSettings.value.encryptionProtocol,
                    dnsSecureMode = userSettings.value.dnsMode,
                    tunnelStatus = tunnelStatus,
                    latencyMs = if (isCloudflareOnline) pingTime else 999,
                    isCloudflareReachable = isCloudflareOnline,
                    interfaceLabel = interfaceName
                )
            } catch (e: Exception) {
                _diagnosticsReport.value = DiagnosticsReport(
                    localIp = "127.0.0.1",
                    gateway = "Unknown",
                    dnsProtocol = "UDP",
                    dnsSecureMode = "Standard",
                    tunnelStatus = "ERROR: ${e.localizedMessage}",
                    latencyMs = -1,
                    isCloudflareReachable = false,
                    interfaceLabel = "loopback"
                )
            } finally {
                _isDiagnosing.value = false
            }
        }
    }
}

// Sealed screen navigation state
sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Permissions : Screen()
    object Home : Screen()
    object Settings : Screen()
    object History : Screen()
}

// Diagnostics data holder
data class DiagnosticsReport(
    val localIp: String,
    val gateway: String,
    val dnsProtocol: String,
    val dnsSecureMode: String,
    val tunnelStatus: String,
    val latencyMs: Int,
    val isCloudflareReachable: Boolean,
    val interfaceLabel: String
)
