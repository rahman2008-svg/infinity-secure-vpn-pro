package com.example.vpn

sealed class VpnState {
    object Disconnected : VpnState()
    object Connecting : VpnState()
    data class Connected(
        val serverCountry: String,
        val serverCode: String,
        val dnsMode: String,
        val connectionMode: String,
        val startTime: Long
    ) : VpnState()
}
