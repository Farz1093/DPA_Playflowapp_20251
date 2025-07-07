package com.esan.payflowapp.data.local.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esan.payflowapp.PayFlowApplication
import com.esan.payflowapp.data.local.entities.SyncMetadata
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class SyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val db        = PayFlowApplication.database
    private val txDao     = db.transactionDao()
    private val metaDao   = db.syncMetadataDao()
    private val fs        = FirebaseFirestore.getInstance()
    private val metaKey   = "lastSync"

    override suspend fun doWork(): Result {
        try {
            // 1) Lee timestamp de última sincronización
            val lastSync = metaDao.getValue(metaKey)?.toLongOrNull() ?: 0L

            // 2) Sube pendientes locales a Firestore
            val pending = txDao.getPending().first()
            pending.forEach { tx ->
                fs.collection("transactions")
                    .document(tx.id)
                    .set(tx)  // convierte automáticamente via Gson/Kotlinx
            }

            // 3) Descarga cambios remotos desde lastSync
            val snapshot = fs.collection("transactions")
                .whereGreaterThan("updatedAt", lastSync)
                .get()
                .await()

            val remoteList = snapshot.toObjects(TransactionEntity::class.java)
            remoteList.forEach { db.transactionDao().upsert(it) }

            // 4) Actualiza metadata
            val now = System.currentTimeMillis()
            metaDao.upsert(SyncMetadata(metaKey, now.toString()))

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

}