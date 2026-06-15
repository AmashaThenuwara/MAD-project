package com.example.agriscout.data.repository

import com.example.agriscout.data.local.dao.DiseaseReportDao
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import kotlinx.coroutines.flow.Flow

class DiseaseReportRepository(private val reportDao: DiseaseReportDao) {

    val allReports: Flow<List<DiseaseReportEntity>> = reportDao.getAllReports()
    val totalReportCount: Flow<Int> = reportDao.getTotalReportCount()
    val pendingSyncCount: Flow<Int> = reportDao.getPendingSyncCount()
    val syncedCount: Flow<Int> = reportDao.getSyncedCount()

    fun getReportsForFarm(farmId: Long): Flow<List<DiseaseReportEntity>> =
        reportDao.getReportsForFarm(farmId)

    suspend fun insertReport(report: DiseaseReportEntity): Long = reportDao.insertReport(report)

    suspend fun updateReport(report: DiseaseReportEntity) = reportDao.updateReport(report)

    suspend fun deleteReport(report: DiseaseReportEntity) = reportDao.deleteReport(report)

    suspend fun getUnsyncedReports(): List<DiseaseReportEntity> = reportDao.getUnsyncedReports()

    suspend fun markReportAsSynced(reportId: Long) = reportDao.markReportAsSynced(reportId)
}
