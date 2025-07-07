package com.esan.payflowapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.esan.payflowapp.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tx: TransactionEntity)

    @Query("SELECT * FROM TransactionEntity WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity WHERE status = 'PENDING'")
    fun getPending(): Flow<List<TransactionEntity>>
}