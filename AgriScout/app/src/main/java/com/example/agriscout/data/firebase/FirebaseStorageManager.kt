package com.example.agriscout.data.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: "anonymous"

    suspend fun uploadDiseaseImage(imageFile: File): Result<String> {
        return try {
            val storageRef = storage.reference
                .child("disease_images/$userId/${imageFile.name}")
            val uploadTask = storageRef.putFile(Uri.fromFile(imageFile)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
