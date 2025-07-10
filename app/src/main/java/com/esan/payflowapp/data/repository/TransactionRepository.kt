package com.esan.payflowapp.data.repository

import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.data.local.dao.TransactionDao
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await


class TransactionRepository(

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {


    /** Crea un depósito local y Firestore lo recogerá por el SyncWorker */
    suspend fun deposit(tx: TransactionEntity): Result<Unit> {
        return try {
            val uid = FirebaseAuthManager.getCurrentUserUid()!!

            // Prepara timestamps
            val now = System.currentTimeMillis()
            val data = tx.copy(
                userId    = uid,
                createdAt = now,
                updatedAt = now
            )

            // Escribe en Firestore
            firestore.collection("transactions")
                .document(data.id)
                .set(data)   // Firestore mapea automáticamente el data class
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun validateTransaction(
        txId: String,
        approve: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val uid = FirebaseAuthManager.getCurrentUserUid()
                ?: throw IllegalStateException("Usuario no autenticado")
            val newStatus = if (approve) "COMPLETED" else "FAILED"
            val now = System.currentTimeMillis()
            firestore.collection("transactions")
                .document(txId)
                .update(
                    mapOf(
                        "status"      to newStatus,
                        "validatedBy" to uid,
                        "validatedAt" to now
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}