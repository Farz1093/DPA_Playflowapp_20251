package com.esan.payflowapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserEntity")
data class UserEntity (
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: Long
)