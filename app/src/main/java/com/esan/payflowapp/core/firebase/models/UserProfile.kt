package com.esan.payflowapp.core.firebase.models

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val createdAt: Long = 0L
)
