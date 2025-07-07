package com.esan.payflowapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TransactionEntity",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId","status","createdAt"])
    ]
)
data class TransactionEntity (
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val amount: Long,
    val status: String,
    val createdAt: Long,

    // the new ones with defaults:
    val currency: String           = "PEN",
    val receiptUrl: String?        = null,
    val description: String?       = null,
    val updatedAt: Long?           = null,
    val validatedBy: String?       = null,
    val validatedAt: Long?         = null,
)