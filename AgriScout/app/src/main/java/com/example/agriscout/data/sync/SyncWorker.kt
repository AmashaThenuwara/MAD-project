package com.example.agriscout.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.agriscout.data.firebase.FirebaseStorageManager
import com.example.agriscout.data.firebase.FirestoreManager
import com.example.agriscout.data.local.AppDatabase
import com.example.agriscout.data.repository.DiseaseReportRepository
import com.example.agriscout.data.repository.FarmRepository
import java.io.File

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val farmRepo = FarmRepository(db.farmDao())
            val reportRepo = DiseaseReportRepository(db.diseaseReportDao())
            val firestoreManager = FirestoreManager()
            val storageManager = FirebaseStorageManager()

            // Sync unsynced farms
            val unsyncedFarms = farmRepo.getUnsyncedFarms()
            for (farm in unsyncedFarms) {
                val result = firestoreManager.uploadFarm(farm)
                if (result.isSuccess) {
                    farmRepo.markFarmAsSynced(farm.farmId)
                }
            }

            // Sync unsynced disease reports
            val unsyncedReports = reportRepo.getUnsyncedReports()
            for (report in unsyncedReports) {
                var imageDownloadUrl = ""

                // Upload image to Firebase Storage if it exists
                if (report.imagePath.isNotEmpty()) {
                    val imageFile = File(report.imagePath)
                    if (imageFile.exists()) {
                        val uploadResult = storageManager.uploadDiseaseImage(imageFile)
                        if (uploadResult.isSuccess) {
                            imageDownloadUrl = uploadResult.getOrDefault("")
                        }
                    }
                }

                // Upload report document to Firestore
                val result = firestoreManager.uploadReport(report, imageDownloadUrl)
                if (result.isSuccess) {
                    reportRepo.markReportAsSynced(report.reportId)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
