package com.axiom.wificontrol

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_history")
data class DeviceHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ip: String,
    val mac: String,
    val vendor: String,
    val event: String,  // "CONNECT" atau "DISCONNECT"
    val timestamp: Long = System.currentTimeMillis()
)
