package com.esan.payflowapp.core.firebase.models

import com.esan.payflowapp.data.local.entities.TransactionEntity

data class TransactionWithUser(
    val transaction: TransactionEntity,
    val userName: String)
