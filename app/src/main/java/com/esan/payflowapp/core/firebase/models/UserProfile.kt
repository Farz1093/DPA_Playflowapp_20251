package com.esan.payflowapp.core.firebase.models

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val createdAt: Long = 0L
)
