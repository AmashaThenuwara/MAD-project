package com.example.agriscout.data.local.dao

import androidx.room.*
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseReportDao {
    @Query("SELECT * FROM disease_reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<DiseaseReportEntity>>

    @Query("SELECT * FROM disease_reports WHERE farmId = :farmId ORDER BY date DESC")
    fun getReportsForFarm(farmId: Long): Flow<List<DiseaseReportEntity>>

    @Query("SELECT * FROM disease_reports WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedReports(): List<DiseaseReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: DiseaseReportEntity): Long

    @Update
    suspend fun updateReport(report: DiseaseReportEntity)

    @Delete
    suspend fun deleteReport(report: DiseaseReportEntity)

    @Query("UPDATE disease_reports SET syncStatus = 'SYNCED' WHERE reportId = :reportId")
    suspend fun markReportAsSynced(reportId: Long)

    @Query("SELECT COUNT(*) FROM disease_reports")
    fun getTotalReportCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM disease_reports WHERE syncStatus = 'PENDING'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM disease_reports WHERE syncStatus = 'SYNCED'")
    fun getSyncedCount(): Flow<Int>
}
