package com.esan.payflowapp.data.repository

import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.firebase.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getProfile(): UserProfile =
        FirebaseAuthManager.getUserProfile()

    fun listAllAdmins(): Flow<List<UserProfile>> = flow {
        val snapshot = firestore.collection("users")
            .whereEqualTo("role", "ADMIN")
            .get()
            .await()
        emit(snapshot.documents.map { doc ->
            UserProfile(
                id        = doc.id,
                name      = doc.getString("name").orEmpty(),
                email     = doc.getString("email").orEmpty(),
                role      = doc.getString("role").orEmpty(),
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        })
    }

    fun listAllUsers(): Flow<List<UserProfile>> = flow {
        try {
            val snapshot = firestore.collection("users")
                .get()       // <-- ojo, .get() en lugar de “snapshots()” o listener
                .await()

            val list = snapshot.documents.map { doc ->
                UserProfile(
                    id        = doc.id,
                    name      = doc.getString("name").orEmpty(),
                    email     = doc.getString("email").orEmpty(),
                    role      = doc.getString("role").orEmpty(),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
            emit(list)
        } catch (e: Exception) {
            // Para que no explote tu ViewModel al recibir PERMISSION_DENIED
            throw e
        }
    }

    /** Crea una cuenta en Firebase Auth y su perfil en Firestore */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String
    ): Result<Unit> = FirebaseAuthManager.registerUser(email, password, name, role)
}