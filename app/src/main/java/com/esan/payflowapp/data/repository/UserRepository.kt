package com.esan.payflowapp.data.repository

import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.firebase.models.UserProfile

class UserRepository {

    suspend fun getProfile(): UserProfile =
        FirebaseAuthManager.getUserProfile()

}