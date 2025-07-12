package com.esan.payflowapp.core.firebase.model

data class TransactionWithUser(
    val transaction: DepositTransaction,
    val userName: String
)
