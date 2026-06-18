package com.example.agriscout.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.agriscout.data.local.dao.CropDao
import com.example.agriscout.data.local.dao.DiseaseReportDao
import com.example.agriscout.data.local.dao.FarmDao
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity

@Database(
    entities = [FarmEntity::class, DiseaseReportEntity::class, CropEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun diseaseReportDao(): DiseaseReportDao
    abstract fun cropDao(): CropDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agri_scout_database"
                )
                    .fallbackToDestructiveMigration() // Clears data on schema change for dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}