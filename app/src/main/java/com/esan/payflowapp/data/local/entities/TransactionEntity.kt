package com.esan.payflowapp.data.local.entities

import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

data class TransactionEntity (
    @PrimaryKey
    val id: String        = "",        // <- valor por defecto
    val userId: String    = "",        // <- valor por defecto
    val type: String      = "",
    val amount: Long      = 0L,
    val status: String    = "",
    val createdAt: Long   = 0L,

    // campos ya tenían valores por defecto
    val currency: String  = "PEN",
    val receiptUrl: String?   = null,
    val description: String?  = null,
    val updatedAt: Long?      = null,
    val validatedBy: String?  = null,
    val validatedAt: Long?    = null,
    val timestamp: Timestamp = Timestamp.now()
)