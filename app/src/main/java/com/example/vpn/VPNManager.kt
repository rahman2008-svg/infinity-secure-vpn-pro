package com.example.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService

object VPNManager {

    /**
     * Checks if VPN permission is granted.
     * If not, returns the Intent that must be launched using ActivityResultLauncher.
     * If granted, returns null.
     */
    fun prepareVpn(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    /**
     * Connects to the VPN.
     */
    fun connect(
        context: Context,
        serverCountry: String,
        serverCode: String,
        dnsMode: String,
        connectionMode: String
    ) {
        val intent = Intent(context, VPNForegroundService::class.java).apply {
            action = VPNForegroundService.ACTION_CONNECT
            putExtra(VPNForegroundService.EXTRA_COUNTRY, serverCountry)
            putExtra(VPNForegroundService.EXTRA_CODE, serverCode)
            putExtra(VPNForegroundService.EXTRA_DNS_MODE, dnsMode)
            putExtra(VPNForegroundService.EXTRA_CONNECTION_MODE, connectionMode)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Disconnects from the VPN.
     */
    fun disconnect(context: Context) {
        val intent = Intent(context, VPNForegroundService::class.java).apply {
            action = VPNForegroundService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}
