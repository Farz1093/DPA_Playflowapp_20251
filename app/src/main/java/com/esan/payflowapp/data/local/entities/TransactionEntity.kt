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
    val type: String,       // "DEPOSIT" | "WITHDRAW"
    val amount: Double,
    val currency: String,
    val status: String,     // "PENDING","APPROVED",…
    val receiptUrl: String?,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val validatedBy: String?,
    val validatedAt: Long?
)