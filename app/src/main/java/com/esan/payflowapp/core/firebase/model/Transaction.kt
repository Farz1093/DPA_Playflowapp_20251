package com.esan.payflowapp.core.firebase.model

data class Transaction(
    val type: String, // "deposit" o "transfer"
    val isValidated: Boolean,
    val isApproved: Boolean,
    val amount: Double,
    val date: Long
) {
    fun isDeposit(): Boolean {
        return type == "deposit"
    }

    fun getTrxType(): String {
        return when (type) {
            "deposit" -> "Depósito"
            "transfer_sent" -> "Transferencia enviada"
            "transfer_received" -> "Transferencia recibida"
            else -> ""
        }
    }

    fun getDepositStatus(): String {
        return if (!isValidated) "No validado" else {
            if (isApproved) "Aprobado" else "Rechazado"
        }
    }
}
