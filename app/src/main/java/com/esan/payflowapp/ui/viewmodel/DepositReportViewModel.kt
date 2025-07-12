package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration // <<< IMPORTANTE: Para el listener
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// El data class de DepositEntity se mantiene igual, con el campo 'validatedAt' como nulo
data class DepositEntity(
    val status: String = "PENDING",
    val amount: Double = 0.0,
    val createdAt: Long = 0L,
    val validatedAt: Long? = null
)

// El data class de DepositReportData también se mantiene igual
data class DepositReportData(
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val totalAmountCompleted: Double = 0.0,
    val totalAmountPending: Double = 0.0,
    val totalAmountFailed: Double = 0.0,
    val averageValidationTimeHours: Double = 0.0
)

sealed class ReportUiState {
    object Loading : ReportUiState()
    data class Success(val data: DepositReportData) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}


class DepositReportViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // <<< CAMBIO 1: Guardamos una referencia al listener para poder cerrarlo después >>>
    private var reportListener: ListenerRegistration? = null

    init {
        // En lugar de llamar a una función de una sola vez, iniciamos la escucha.
        listenForReportUpdates()
    }

    private fun listenForReportUpdates() {
        _uiState.value = ReportUiState.Loading

        val query = firestore.collection("deposit_data")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(500)

        // <<< CAMBIO 2: Usamos addSnapshotListener en lugar de get().await() >>>
        reportListener = query.addSnapshotListener { snapshot, error ->

            // Si hay un error de Firebase, lo mostramos en la UI.
            if (error != null) {
                _uiState.value = ReportUiState.Error(error.message ?: "Error al escuchar los datos.")
                return@addSnapshotListener
            }

            // Si el snapshot es nulo (raro, pero posible), no hacemos nada.
            if (snapshot == null) {
                return@addSnapshotListener
            }

            // Toda la lógica de cálculo que teníamos antes, ahora se ejecuta CADA VEZ que hay un cambio.
            try {
                val deposits = snapshot.toObjects(DepositEntity::class.java)

                var pendingCount = 0
                var completedCount = 0
                var failedCount = 0
                var totalAmountCompleted = 0.0
                var totalAmountPending = 0.0
                var totalAmountFailed = 0.0
                var totalValidationTimeMillis = 0L
                var validatedDepositsCount = 0

                for (deposit in deposits) {
                    when (deposit.status) {
                        "PENDING" -> {
                            pendingCount++
                            totalAmountPending += deposit.amount
                        }
                        "COMPLETED" -> {
                            completedCount++
                            totalAmountCompleted += deposit.amount
                            val validatedAt = deposit.validatedAt
                            if (validatedAt != null && validatedAt > 0 && deposit.createdAt > 0) {
                                totalValidationTimeMillis += (validatedAt - deposit.createdAt)
                                validatedDepositsCount++
                            }
                        }
                        "FAILED" -> {
                            failedCount++
                            totalAmountFailed += deposit.amount
                        }
                    }
                }

                val averageTimeMillis = if (validatedDepositsCount > 0) totalValidationTimeMillis / validatedDepositsCount else 0L
                val averageTimeHours = averageTimeMillis / (1000.0 * 60 * 60)

                val reportData = DepositReportData(
                    pendingCount = pendingCount,
                    completedCount = completedCount,
                    failedCount = failedCount,
                    totalAmountCompleted = totalAmountCompleted,
                    totalAmountPending = totalAmountPending,
                    totalAmountFailed = totalAmountFailed,
                    averageValidationTimeHours = averageTimeHours
                )

                _uiState.value = ReportUiState.Success(reportData)

            } catch (e: Exception) {
                // Captura cualquier error durante la conversión de datos (como el que ya arreglamos)
                _uiState.value = ReportUiState.Error(e.message ?: "Error al procesar los datos.")
            }
        }
    }

    // <<< CAMBIO 3: Es MUY importante limpiar el listener para evitar fugas de memoria >>>
    override fun onCleared() {
        super.onCleared()
        // Cuando el ViewModel ya no se use (ej: el usuario cierra la pantalla),
        // dejamos de escuchar a la base de datos.
        reportListener?.remove()
    }
}