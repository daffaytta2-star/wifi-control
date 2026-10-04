package com.axiom.wificontrol

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(entry: DeviceHistory)

    @Query("SELECT * FROM device_history ORDER BY timestamp DESC LIMIT 100")
    suspend fun getAll(): List<DeviceHistory>

    @Query("DELETE FROM device_history")
    suspend fun clearAll()
}
