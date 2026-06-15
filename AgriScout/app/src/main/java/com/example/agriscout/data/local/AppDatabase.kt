package com.example.agriscout.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.agriscout.data.local.dao.DiseaseReportDao
import com.example.agriscout.data.local.dao.FarmDao
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity

@Database(
    entities = [FarmEntity::class, DiseaseReportEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun diseaseReportDao(): DiseaseReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2: adds new columns to farms + creates disease_reports table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to farms table
                database.execSQL("ALTER TABLE farms ADD COLUMN fieldSizeHectares REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE farms ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE farms ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE farms ADD COLUMN createdDate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE farms ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                // Create disease_reports table
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `disease_reports` (
                        `reportId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `farmId` INTEGER NOT NULL,
                        `diseaseName` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `imagePath` TEXT NOT NULL DEFAULT '',
                        `latitude` REAL NOT NULL DEFAULT 0.0,
                        `longitude` REAL NOT NULL DEFAULT 0.0,
                        `syncStatus` TEXT NOT NULL DEFAULT 'PENDING',
                        `date` INTEGER NOT NULL
                    )"""
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agri_scout_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
