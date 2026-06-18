package com.example.agriscout.data.repository

import com.example.agriscout.data.local.dao.CropDao
import com.example.agriscout.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

class CropRepository(private val cropDao: CropDao) {
    val allCrops: Flow<List<CropEntity>> = cropDao.getAllCrops()

    fun getCropsForFarm(farmId: Long): Flow<List<CropEntity>> =
        cropDao.getCropsForFarm(farmId)

    suspend fun insertCrop(crop: CropEntity): Long = cropDao.insertCrop(crop)

    suspend fun updateCrop(crop: CropEntity) = cropDao.updateCrop(crop)

    suspend fun deleteCrop(crop: CropEntity) = cropDao.deleteCrop(crop)

    suspend fun getCropById(cropId: Long): CropEntity? = cropDao.getCropById(cropId)

    suspend fun getUnsyncedCrops(): List<CropEntity> = cropDao.getUnsyncedCrops()

    suspend fun markCropAsSynced(cropId: Long) = cropDao.markCropAsSynced(cropId)
}
