package com.esan.payflowapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.esan.payflowapp.data.local.entities.SyncMetadata

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: SyncMetadata)

    @Query("SELECT value FROM SyncMetadata WHERE key = :key")
    suspend fun getValue(key: String): String?
}