package com.esan.payflowapp.core.firebase.model

data class UserData(
    var name: String,
    var accountNumber: String,
    var isAdmin: Boolean,
    var balance: Double
)
