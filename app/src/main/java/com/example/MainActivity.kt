package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val userSettings by viewModel.userSettings.collectAsState()
                    val connectionLogs by viewModel.connectionLogs.collectAsState()
                    val trustedNetworks by viewModel.trustedNetworks.collectAsState()
                    val diagnosticsReport by viewModel.diagnosticsReport.collectAsState()
                    val isDiagnosing by viewModel.isDiagnosing.collectAsState()

                    // Real-time telemetry from service
                    val vpnState by viewModel.vpnState.collectAsState()
                    val bytesUploaded by viewModel.bytesUploaded.collectAsState()
                    val bytesDownloaded by viewModel.bytesDownloaded.collectAsState()
                    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
                    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
                    val currentPing by viewModel.currentPing.collectAsState()
                    val activeServer by viewModel.activeServer.collectAsState()
                    val activeServerCode by viewModel.activeServerCode.collectAsState()

                    // Crossfade provides extremely fluid screen-to-screen transitions
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(400),
                        label = "MainScreenNavigation"
                    ) { screen ->
                        when (screen) {
                            Screen.Splash -> SplashScreen()
                            Screen.Onboarding -> OnboardingScreen(
                                onComplete = { viewModel.setOnboarded() }
                            )
                            Screen.Permissions -> PermissionsScreen(
                                onPermissionsGranted = { viewModel.completePermissions() }
                            )
                            Screen.Home -> HomeScreen(
                                vpnState = vpnState,
                                bytesUploaded = bytesUploaded,
                                bytesDownloaded = bytesDownloaded,
                                uploadSpeed = uploadSpeed,
                                downloadSpeed = downloadSpeed,
                                currentPing = currentPing,
                                activeServer = activeServer,
                                activeServerCode = activeServerCode,
                                selectedMode = userSettings.selectedMode,
                                dnsMode = userSettings.dnsMode,
                                onToggleVpn = { viewModel.toggleVpn(this@MainActivity) },
                                onModeSelected = { viewModel.updateSelectedMode(it) },
                                onServerSelected = { viewModel.selectServer(it) },
                                onNavigateSettings = { viewModel.navigateTo(Screen.Settings) },
                                onNavigateHistory = { viewModel.navigateTo(Screen.History) }
                            )
                            Screen.Settings -> SettingsScreen(
                                autoConnect = userSettings.autoConnect,
                                dnsMode = userSettings.dnsMode,
                                encryptionProtocol = userSettings.encryptionProtocol,
                                trustedNetworks = trustedNetworks,
                                diagnosticsReport = diagnosticsReport,
                                isDiagnosing = isDiagnosing,
                                onBack = { viewModel.navigateTo(Screen.Home) },
                                onToggleAutoConnect = { viewModel.toggleAutoConnect(it) },
                                onDnsModeSelected = { viewModel.updateDnsMode(it) },
                                onEncryptionSelected = { viewModel.updateEncryptionProtocol(it) },
                                onAddTrustedNetwork = { viewModel.addTrustedNetwork(it) },
                                onRemoveTrustedNetwork = { viewModel.removeTrustedNetwork(it) },
                                onRunDiagnostics = { viewModel.diagnoseNetwork(this@MainActivity) }
                            )
                            Screen.History -> HistoryScreen(
                                logs = connectionLogs,
                                onBack = { viewModel.navigateTo(Screen.Home) },
                                onClearAll = { viewModel.clearHistory() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        // Back press handling for state-based screen stack
        when (viewModel.currentScreen.value) {
            Screen.Settings, Screen.History -> {
                viewModel.navigateTo(Screen.Home)
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}
