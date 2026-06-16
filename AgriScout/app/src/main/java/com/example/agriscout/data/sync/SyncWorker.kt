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

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {

        Log.d("SyncWorker", "===== SYNC STARTED =====")

        return try {

            val db = AppDatabase.getDatabase(applicationContext)

            val farmRepo = FarmRepository(db.farmDao())
            val reportRepo = DiseaseReportRepository(db.diseaseReportDao())

            val firestoreManager = FirestoreManager()
            val storageManager = FirebaseStorageManager()

            var progressMade = false

            // -----------------------------
            // STEP 1 - Upload Farms
            // -----------------------------
            val unsyncedFarms = farmRepo.getUnsyncedFarms()

            Log.d(
                "SyncWorker",
                "Unsynced farms count: ${unsyncedFarms.size}"
            )

            for (farm in unsyncedFarms) {

                try {

                    val result = withTimeoutOrNull(15000) {
                        firestoreManager.uploadFarm(farm)
                    }

                    if (result?.isSuccess == true) {

                        farmRepo.markFarmAsSynced(farm.farmId)

                        progressMade = true

                        Log.d(
                            "SyncWorker",
                            "Farm synced: ${farm.farmName}"
                        )

                    } else {

                        Log.e(
                            "SyncWorker",
                            "Farm upload failed: ${farm.farmName}"
                        )
                    }

                } catch (e: Exception) {

                    Log.e(
                        "SyncWorker",
                        "Farm upload exception",
                        e
                    )
                }
            }

            // -----------------------------
            // STEP 2 - Upload Reports
            // -----------------------------
            val unsyncedReports = reportRepo.getUnsyncedReports()

            Log.d(
                "SyncWorker",
                "Unsynced reports count: ${unsyncedReports.size}"
            )

            for (report in unsyncedReports) {

                try {

                    var cloudImageUrl = ""

                    if (
                        report.imagePath.isNotBlank() &&
                        !report.imagePath.startsWith("http")
                    ) {

                        val imageFile = File(report.imagePath)

                        if (imageFile.exists()) {

                            Log.d(
                                "SyncWorker",
                                "Uploading image: ${imageFile.name}"
                            )

                            val imageResult = withTimeoutOrNull(45000) {
                                storageManager.uploadDiseaseImage(imageFile)
                            }

                            cloudImageUrl =
                                imageResult?.getOrNull() ?: ""

                        } else {

                            Log.e(
                                "SyncWorker",
                                "Image file not found: ${report.imagePath}"
                            )
                        }
                    }

                    val reportResult = withTimeoutOrNull(15000) {
                        firestoreManager.uploadReport(
                            report,
                            cloudImageUrl
                        )
                    }

                    if (reportResult?.isSuccess == true) {

                        reportRepo.markReportAsSynced(
                            report.reportId
                        )

                        progressMade = true

                        Log.d(
                            "SyncWorker",
                            "Report synced: ${report.diseaseName}"
                        )

                    } else {

                        Log.e(
                            "SyncWorker",
                            "Report upload failed: ${report.diseaseName}"
                        )
                    }

                } catch (e: Exception) {

                    Log.e(
                        "SyncWorker",
                        "Report upload exception",
                        e
                    )
                }
            }

            // -----------------------------
            // STEP 3 - Download Farms
            // -----------------------------
            try {

                val farmsResult =
                    firestoreManager.getAllFarms()

                if (farmsResult.isSuccess) {

                    farmsResult.getOrNull()?.forEach { farm ->
                        farmRepo.insertFarm(farm)
                    }

                    Log.d(
                        "SyncWorker",
                        "Cloud farms downloaded"
                    )

                } else {

                    Log.e(
                        "SyncWorker",
                        "Failed loading farms from cloud",
                        farmsResult.exceptionOrNull()
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "SyncWorker",
                    "Farm download exception",
                    e
                )
            }

            // -----------------------------
            // STEP 4 - Download Reports
            // -----------------------------
            try {

                val reportsResult =
                    firestoreManager.getAllReports()

                if (reportsResult.isSuccess) {

                    reportsResult.getOrNull()?.forEach { report ->
                        reportRepo.insertReport(report)
                    }

                    progressMade = true

                    Log.d(
                        "SyncWorker",
                        "Cloud reports downloaded"
                    )

                } else {

                    Log.e(
                        "SyncWorker",
                        "Failed loading reports from cloud",
                        reportsResult.exceptionOrNull()
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "SyncWorker",
                    "Report download exception",
                    e
                )
            }

            Log.d(
                "SyncWorker",
                "===== SYNC COMPLETED (Progress: $progressMade) ====="
            )

            // IMPORTANT:
            // Don't return FAILURE just because
            // there was nothing to sync.

            ListenableWorker.Result.success()

        } catch (e: Exception) {

            Log.e(
                "SyncWorker",
                "FATAL SYNC ERROR",
                e
            )

            ListenableWorker.Result.retry()
        }
    }
}
