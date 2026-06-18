package com.example.agriscout.data.repository

import com.example.agriscout.data.local.dao.FarmDao
import com.example.agriscout.data.local.entity.FarmEntity
import kotlinx.coroutines.flow.Flow

class FarmRepository(private val farmDao: FarmDao) {

    val allFarms: Flow<List<FarmEntity>> = farmDao.getAllFarms()
    val totalFarmCount: Flow<Int> = farmDao.getTotalFarmCount()

    suspend fun insertFarm(farm: FarmEntity): Long = farmDao.insertFarm(farm)

    suspend fun updateFarm(farm: FarmEntity) = farmDao.updateFarm(farm)

    suspend fun deleteFarm(farm: FarmEntity) = farmDao.deleteFarm(farm)

    fun getFarmById(farmId: Long): Flow<FarmEntity?> = farmDao.getFarmById(farmId)

    suspend fun getUnsyncedFarms(): List<FarmEntity> = farmDao.getUnsyncedFarms()

    suspend fun markFarmAsSynced(farmId: Long) = farmDao.markFarmAsSynced(farmId)
}
