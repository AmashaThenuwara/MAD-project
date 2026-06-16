package com.example.agriscout.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.agriscout.data.firebase.FirebaseStorageManager
import com.example.agriscout.data.firebase.FirestoreManager
import com.example.agriscout.data.local.AppDatabase
import com.example.agriscout.data.repository.DiseaseReportRepository
import com.example.agriscout.data.repository.FarmRepository
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting FINAL Resilience Sync...")
        var progressMade = false
        var lastError = "None"

        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val farmRepo = FarmRepository(db.farmDao())
            val reportRepo = DiseaseReportRepository(db.diseaseReportDao())
            val firestoreManager = FirestoreManager()
            val storageManager = FirebaseStorageManager()

            // --- STEP 1: UPLOAD FARMS ---
            val unsyncedFarms = farmRepo.getUnsyncedFarms()
            for (farm in unsyncedFarms) {
                val result = withTimeoutOrNull(15000) { firestoreManager.uploadFarm(farm) }
                if (result?.isSuccess == true) {
                    farmRepo.markFarmAsSynced(farm.farmId)
                    progressMade = true
                    Log.d("SyncWorker", "Farm ${farm.farmName} marked SYNCED locally")
                }
            }

            // --- STEP 2: UPLOAD REPORTS ---
            val unsyncedReports = reportRepo.getUnsyncedReports()
            for (report in unsyncedReports) {
                var cloudImageUrl = ""
                if (report.imagePath.isNotEmpty() && !report.imagePath.startsWith("http")) {
                    val imageFile = File(report.imagePath)
                    if (imageFile.exists()) {
                        val imgResult = withTimeoutOrNull(45000) { storageManager.uploadDiseaseImage(imageFile) }
                        cloudImageUrl = imgResult?.getOrNull() ?: ""
                    }
                }

                val result = withTimeoutOrNull(15000) { firestoreManager.uploadReport(report, cloudImageUrl) }
                if (result?.isSuccess == true) {
                    reportRepo.markReportAsSynced(report.reportId)
                    progressMade = true
                    Log.d("SyncWorker", "Report ${report.diseaseName} marked SYNCED locally")
                } else {
                    lastError = result?.exceptionOrNull()?.message ?: "Upload timed out"
                }
            }

            // --- STEP 3: FORCE RECONCILE FROM CLOUD ---
            // This pulls everything from Firebase and overwrites local "PENDING" status
            val cloudFarms = firestoreManager.getAllFarms().getOrNull()
            cloudFarms?.forEach { farmRepo.insertFarm(it) }

            val cloudReports = firestoreManager.getAllReports().getOrNull()
            if (cloudReports != null) {
                cloudReports.forEach { reportRepo.insertReport(it) }
                progressMade = true
            }

            if (progressMade) {
                Log.d("SyncWorker", "Sync Finished: Progress was made.")
                Result.success()
            } else {
                Log.w("SyncWorker", "Sync Finished: No changes needed or errors occurred.")
                Result.failure(Data.Builder().putString("error", lastError).build())
            }

        } catch (e: Exception) {
            Log.e("SyncWorker", "FATAL SYNC ERROR: ${e.message}")
            Result.retry()
        }
    }
}
