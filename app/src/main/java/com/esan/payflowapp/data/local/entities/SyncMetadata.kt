package com.esan.payflowapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SyncMetadata")
data class SyncMetadata (
    @PrimaryKey val key: String,
    val value: String
)