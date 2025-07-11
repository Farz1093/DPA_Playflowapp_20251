package com.esan.payflowapp.ui.viewmodel

import com.esan.payflowapp.core.firebase.models.TransactionWithUser
import com.esan.payflowapp.data.local.entities.TransactionEntity


sealed class ValidationUiState {
    object Loading                       : ValidationUiState()
    data class Error(val message:String) : ValidationUiState()
    data class Loaded(val txWithUser: TransactionWithUser) :
        ValidationUiState()
    data class Confirming(
        val txWithUser: TransactionWithUser,
        val action: Action
    ) : ValidationUiState()
    object Success                       : ValidationUiState()
}

enum class Action { APPROVE, REJECT }