package com.example.agriscout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey(autoGenerate = true) val farmId: Long = 0,
    val farmName: String,
    val farmerName: String,
    val locationName: String,
    val landSize: Double = 0.0,
    val locationDetails: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)