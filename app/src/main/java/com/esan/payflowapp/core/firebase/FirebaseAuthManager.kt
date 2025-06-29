package com.esan.payflowapp.core.firebase

import android.annotation.SuppressLint
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

    suspend fun getUserData(): Triple<String, Double, Boolean> {
        var uid = auth.currentUser?.uid.orEmpty()
        var snapshot = db.collection("user_data").document(uid).get().await()
        val name = snapshot.getString("name").orEmpty()
        val isAdmin = snapshot.getBoolean("is_admin") ?: false
        val balance = snapshot.getDouble("balance") ?: 0.0
        return Triple(name, balance, isAdmin)
    }

    suspend fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

}