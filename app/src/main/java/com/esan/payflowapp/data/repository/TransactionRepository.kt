package com.esan.payflowapp.data.repository

import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.data.local.dao.TransactionDao
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await


class TransactionRepository(
    private val txDao: TransactionDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Flujo de las últimas N transacciones locales */
    fun getRecentTransactions(userId: String, limit: Long = 10): Flow<List<TransactionEntity>> =
        txDao.getByUserPaged(userId, limit)

    /** Calcula el saldo sumando income − outcome (puedes implementarlo en Room con @Query) */
    fun getBalance(userId: String): Flow<Long> =
        txDao.getBalance(userId)

    /** Crea un depósito local y Firestore lo recogerá por el SyncWorker */
    suspend fun deposit(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        txDao.upsert(tx.copy(status = "PENDING", createdAt = System.currentTimeMillis()))
    }

    /** Crea un retiro local (valida saldo en ViewModel antes) */
    suspend fun withdraw(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        txDao.upsert(tx.copy(status = "COMPLETED", createdAt = System.currentTimeMillis()))
    }

    // Opcional: método para forzar un push inmediato a Firestore
    suspend fun pushPending() = withContext(Dispatchers.IO) {
        val userId = FirebaseAuthManager.getCurrentUserUid()!!
        val pending = txDao.getPendingOnce(userId)
        pending.forEach { doc ->
            firestore.collection("transactions")
                .document(doc.id)
                .set(doc)
                .await()
        }
    }
}