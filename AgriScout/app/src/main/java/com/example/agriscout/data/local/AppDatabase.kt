package com.example.agriscout.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.agriscout.data.local.dao.DiseaseReportDao
import com.example.agriscout.data.local.dao.FarmDao
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity

@Database(
    entities = [FarmEntity::class, DiseaseReportEntity::class],
    version = 9,          // ← bumped to 9 (higher than 8 on device)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun diseaseReportDao(): DiseaseReportDao

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
                    .fallbackToDestructiveMigration() // ← handles all version mismatches
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}