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

    @Query("""
      SELECT * FROM TransactionEntity
      WHERE userId = :userId
      ORDER BY createdAt DESC
      LIMIT :limit
    """)
    fun getByUserPaged(userId: String, limit: Long): Flow<List<TransactionEntity>>

    @Query("""
      SELECT SUM(
        CASE WHEN type = 'DEPOSIT' THEN amount 
             WHEN type = 'WITHDRAW' THEN -amount 
             ELSE 0 END
      ) FROM TransactionEntity
      WHERE userId = :userId
    """)
    fun getBalance(userId: String): Flow<Long>

    @Query("""
      SELECT * FROM TransactionEntity
      WHERE userId = :userId
        AND status   = 'PENDING'
    """)
    suspend fun getPendingOnce(userId: String): List<TransactionEntity>
}