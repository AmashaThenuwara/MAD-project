package com.example.agriscout.data.local.dao

import androidx.room.*
import com.example.agriscout.data.local.entity.FarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms ORDER BY createdDate DESC")
    fun getAllFarms(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE farmId = :farmId")
    fun getFarmById(farmId: Long): Flow<FarmEntity?>

    @Query("SELECT * FROM farms WHERE isSynced = 0")
    suspend fun getUnsyncedFarms(): List<FarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity): Long

    @Update
    suspend fun updateFarm(farm: FarmEntity)

    @Delete
    suspend fun deleteFarm(farm: FarmEntity)

    @Query("UPDATE farms SET isSynced = 1 WHERE farmId = :farmId")
    suspend fun markFarmAsSynced(farmId: Long)

    @Query("SELECT COUNT(*) FROM farms")
    fun getTotalFarmCount(): Flow<Int>
}
