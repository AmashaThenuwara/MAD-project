package com.example.agriscout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey(autoGenerate = true) val cropId: Long = 0,
    val farmId: Long,
    val commonName: String,
    val scientificName: String = "",
    val category: String = "", // vegetable, fruit, grain, etc.
    val origin: String = "",
    val soilType: String = "",
    val fertilizerType: String = "", // organic or inorganic
    val imagePath: String = "",
    val isSynced: Boolean = false
)