package com.example.agriscout.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.agriscout.data.firebase.FirebaseStorageManager
import com.example.agriscout.data.firebase.FirestoreManager
import com.example.agriscout.data.local.AppDatabase
import com.example.agriscout.data.repository.DiseaseReportRepository
import com.example.agriscout.data.repository.FarmRepository
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class SyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val farmRepo = FarmRepository(db.farmDao())
            val reportRepo = DiseaseReportRepository(db.diseaseReportDao())
            val firestoreManager = FirestoreManager()
            val storageManager = FirebaseStorageManager()

            val cropRepo = com.example.agriscout.data.repository.CropRepository(db.cropDao())

            // Step 1: Upload unsynced farms to Firestore
            farmRepo.getUnsyncedFarms().forEach { farm ->
                val result = withTimeoutOrNull(15000) { firestoreManager.uploadFarm(farm) }
                if (result?.isSuccess == true) farmRepo.markFarmAsSynced(farm.farmId)
            }

            // Step 1.5: Upload unsynced crops to Firestore
            cropRepo.getUnsyncedCrops().forEach { crop ->
                var cloudImageUrl = ""
                if (crop.imagePath.isNotBlank() && !crop.imagePath.startsWith("http")) {
                    val imageFile = File(crop.imagePath)
                    if (imageFile.exists()) {
                        cloudImageUrl = withTimeoutOrNull(45000) { storageManager.uploadDiseaseImage(imageFile) }?.getOrNull() ?: ""
                    }
                }
                val result = withTimeoutOrNull(15000) { firestoreManager.uploadCrop(crop, cloudImageUrl) }
                if (result?.isSuccess == true) cropRepo.markCropAsSynced(crop.cropId)
            }

            // Step 2: Upload unsynced reports and images
            reportRepo.getUnsyncedReports().forEach { report ->
                var cloudImageUrl = ""
                if (report.imagePath.isNotBlank() && !report.imagePath.startsWith("http")) {
                    val imageFile = File(report.imagePath)
                    if (imageFile.exists()) {
                        cloudImageUrl = withTimeoutOrNull(45000) { storageManager.uploadDiseaseImage(imageFile) }?.getOrNull() ?: ""
                    }
                }
                val reportResult = withTimeoutOrNull(15000) { firestoreManager.uploadReport(report, cloudImageUrl) }
                if (reportResult?.isSuccess == true) reportRepo.markReportAsSynced(report.reportId)
            }

            // Step 3: Download updates from cloud
            firestoreManager.getAllFarms().getOrNull()?.forEach { farm -> 
                farmRepo.insertFarm(farm)
                // Also fetch crops for this farm
                firestoreManager.getCropsForFarm(farm.farmId).getOrNull()?.forEach { crop ->
                    cropRepo.insertCrop(crop)
                }
            }
            firestoreManager.getAllReports().getOrNull()?.forEach { reportRepo.insertReport(it) }

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            ListenableWorker.Result.retry()
        }
    }
}
