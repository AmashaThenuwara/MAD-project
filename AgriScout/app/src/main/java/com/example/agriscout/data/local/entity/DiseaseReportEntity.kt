package com.example.agriscout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disease_reports")
data class DiseaseReportEntity(
    @PrimaryKey(autoGenerate = true) val reportId: Long = 0,
    val farmId: Long,
    val diseaseName: String,
    val notes: String,
    val imagePath: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val syncStatus: String = "PENDING", // "PENDING" or "SYNCED"
    val date: Long = System.currentTimeMillis()
)
