package com.example.agriscout.data.local.dao

import androidx.room.*
import com.example.agriscout.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops")
    fun getAllCrops(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE farmId = :farmId")
    fun getCropsForFarm(farmId: Long): Flow<List<CropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity): Long

    @Update
    suspend fun updateCrop(crop: CropEntity)

    @Delete
    suspend fun deleteCrop(crop: CropEntity)

    @Query("SELECT * FROM crops WHERE cropId = :cropId")
    suspend fun getCropById(cropId: Long): CropEntity?

    @Query("SELECT * FROM crops WHERE isSynced = 0")
    suspend fun getUnsyncedCrops(): List<CropEntity>

    @Query("UPDATE crops SET isSynced = 1 WHERE cropId = :cropId")
    suspend fun markCropAsSynced(cropId: Long)
}
