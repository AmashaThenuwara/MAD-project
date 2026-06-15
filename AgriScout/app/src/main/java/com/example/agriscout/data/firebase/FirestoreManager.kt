package com.example.agriscout.data.firebase

import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: "anonymous"

    suspend fun uploadFarm(farm: FarmEntity): Result<String> {
        return try {
            val farmMap = hashMapOf(
                "farmId" to farm.farmId,
                "farmName" to farm.farmName,
                "farmerName" to farm.farmerName,
                "locationName" to farm.locationName,
                "cropType" to farm.cropType,
                "growthStage" to farm.growthStage,
                "fieldSizeHectares" to farm.fieldSizeHectares,
                "latitude" to farm.latitude,
                "longitude" to farm.longitude,
                "createdDate" to farm.createdDate,
                "uploadedBy" to userId
            )
            db.collection("farms")
                .document("${userId}_${farm.farmId}")
                .set(farmMap)
                .await()
            Result.success("Farm uploaded successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadReport(report: DiseaseReportEntity, imageDownloadUrl: String = ""): Result<String> {
        return try {
            val reportMap = hashMapOf(
                "reportId" to report.reportId,
                "farmId" to report.farmId,
                "diseaseName" to report.diseaseName,
                "notes" to report.notes,
                "imagePath" to imageDownloadUrl,
                "latitude" to report.latitude,
                "longitude" to report.longitude,
                "date" to report.date,
                "uploadedBy" to userId
            )
            db.collection("disease_reports")
                .document("${userId}_${report.reportId}")
                .set(reportMap)
                .await()
            Result.success("Report uploaded successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadUserProfile(name: String, email: String): Result<String> {
        return try {
            val userMap = hashMapOf(
                "name" to name,
                "email" to email,
                "uid" to userId
            )
            db.collection("users")
                .document(userId)
                .set(userMap)
                .await()
            Result.success("Profile saved")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
