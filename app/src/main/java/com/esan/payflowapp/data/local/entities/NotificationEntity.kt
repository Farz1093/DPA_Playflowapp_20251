package com.esan.payflowapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "NotificationEntity",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NotificationEntity (
    @PrimaryKey val id: String,
    val userId: String,
    val transactionId: String,
    val type: String,
    val read: Boolean = false,
    val createdAt: Long
)