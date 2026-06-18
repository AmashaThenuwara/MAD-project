package com.example.agriscout.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            // Sign in with email and password
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            // Create new user account
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            
            // Set the display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            
            // Reload user to ensure profile is updated locally
            user.reload().await()
            
            Result.success(auth.currentUser!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
