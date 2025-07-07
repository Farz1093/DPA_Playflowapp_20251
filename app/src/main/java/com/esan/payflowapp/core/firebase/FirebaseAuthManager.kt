package com.esan.payflowapp.core.firebase

import android.annotation.SuppressLint
import com.esan.payflowapp.core.firebase.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    //Login
    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): UserProfile {
        val uid = auth.currentUser!!.uid
        val snap = db
            .collection("users")
            .document(uid)
            .get()
            .await()

        // Mapea directamente a tu data class
        val profile = snap.toObject(UserProfile::class.java)
            ?: throw IllegalStateException("Perfil de usuario sin datos")

        return profile
    }

    suspend fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

}