package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSetting?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettings(): UserSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSetting)

    // Connection Logs
    @Query("SELECT * FROM connection_logs ORDER BY startTime DESC")
    fun getAllConnectionLogs(): Flow<List<ConnectionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectionLog(log: ConnectionLog)

    @Query("DELETE FROM connection_logs")
    suspend fun clearAllLogs()

    // Trusted Networks
    @Query("SELECT * FROM trusted_networks ORDER BY addedTime DESC")
    fun getAllTrustedNetworks(): Flow<List<TrustedNetwork>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrustedNetwork(network: TrustedNetwork)

    @Delete
    suspend fun deleteTrustedNetwork(network: TrustedNetwork)

    @Query("SELECT EXISTS(SELECT 1 FROM trusted_networks WHERE ssid = :ssid LIMIT 1)")
    suspend fun isNetworkTrusted(ssid: String): Boolean
}
