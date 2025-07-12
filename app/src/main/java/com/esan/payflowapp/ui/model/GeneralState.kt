package com.esan.payflowapp.ui.model

sealed class GeneralState {
    object Idle : GeneralState()
    object Loading : GeneralState()
    object Success : GeneralState()
    class Fail(val message: String) : GeneralState()
}