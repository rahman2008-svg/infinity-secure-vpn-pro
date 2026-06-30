package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class VpnRepository(private val dao: SettingsDao) {

    val userSettings: Flow<UserSetting?> = dao.getUserSettingsFlow().flowOn(Dispatchers.IO)

    val connectionLogs: Flow<List<ConnectionLog>> = dao.getAllConnectionLogs().flowOn(Dispatchers.IO)

    val trustedNetworks: Flow<List<TrustedNetwork>> = dao.getAllTrustedNetworks().flowOn(Dispatchers.IO)

    suspend fun getSettings(): UserSetting = withContext(Dispatchers.IO) {
        dao.getUserSettings() ?: UserSetting().also {
            dao.insertUserSettings(it)
        }
    }

    suspend fun updateSettings(updater: (UserSetting) -> UserSetting) = withContext(Dispatchers.IO) {
        val current = dao.getUserSettings() ?: UserSetting()
        val updated = updater(current)
        dao.insertUserSettings(updated)
    }

    suspend fun addConnectionLog(log: ConnectionLog) = withContext(Dispatchers.IO) {
        dao.insertConnectionLog(log)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearAllLogs()
    }

    suspend fun addTrustedNetwork(ssid: String) = withContext(Dispatchers.IO) {
        dao.insertTrustedNetwork(TrustedNetwork(ssid = ssid))
    }

    suspend fun removeTrustedNetwork(ssid: String) = withContext(Dispatchers.IO) {
        dao.deleteTrustedNetwork(TrustedNetwork(ssid = ssid))
    }

    suspend fun isNetworkTrusted(ssid: String): Boolean = withContext(Dispatchers.IO) {
        dao.isNetworkTrusted(ssid)
    }
}
