package com.esan.payflowapp.core.firebase.model

data class UserDataDeposit(
    var name: String = "",
    var accountNumber: String = "",
    var isAdmin: Boolean = false,
    var balance: Double = 0.0
)
