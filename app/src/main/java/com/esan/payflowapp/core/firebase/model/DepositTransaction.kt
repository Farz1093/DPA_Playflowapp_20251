package com.esan.payflowapp.core.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName


data class DepositTransaction(
    @get:PropertyName("uid") @set:PropertyName("uid")
    var uid: String = "",
    var amount: Double = 0.0,
    var date: Timestamp? = null,
    var status: String = "",
    var createdAt: Long = 0L,
    var currency: String = "PEN",
    var type: String = "DEPOSIT",
    var isValidated: Boolean = false,
    var isApproved: Boolean = false,
    var id: String = "" ,
    val updatedAt: Long?      = null,
    val validatedBy: String?  = null,
    val validatedAt: Long?    = null,
    val timestamp: Timestamp = Timestamp.now()
)
