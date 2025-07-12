package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.firebase.model.DepositTransaction
import com.esan.payflowapp.core.firebase.model.TransactionWithUser
import com.esan.payflowapp.core.firebase.model.UserDataDeposit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- Clases de Estado y Acción (actualizadas para usar TransactionWithUser) ---
sealed interface ValidationUiState {
    object Loading : ValidationUiState
    data class Loaded(val txWithUser: TransactionWithUser) : ValidationUiState
    data class Confirming(val txWithUser: TransactionWithUser, val action: Action) : ValidationUiState
    data class Error(val message: String) : ValidationUiState
    object Success : ValidationUiState
}

enum class Action {
    APPROVE, REJECT
}

// --- ViewModel ---
class DepositValidationViewModel(
    private val txId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ValidationUiState>(ValidationUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val firestore = FirebaseFirestore.getInstance()

    fun load() {
        viewModelScope.launch {
            _uiState.value = ValidationUiState.Loading
            try {
                // Paso 1: Obtener la transacción desde Firestore usando su ID
                val transactionDoc = firestore.collection("deposit_data").document(txId).get().await()
                val transaction = transactionDoc.toObject(DepositTransaction::class.java)?.copy(id = transactionDoc.id)
                    ?: throw Exception("Transacción no encontrada con ID: $txId")

                // Paso 2: Usar el 'uid' de la transacción para obtener el documento del usuario
                val userDoc = firestore.collection("user_data").document(transaction.uid).get().await()
                val user = userDoc.toObject(UserDataDeposit::class.java)

                // Paso 3: Combinar los datos en un solo objeto
                val txWithUser = TransactionWithUser(
                    transaction = transaction,
                    userName = user?.name ?: "Usuario Desconocido"
                )

                _uiState.value = ValidationUiState.Loaded(txWithUser)

            } catch (e: Exception) {
                _uiState.value = ValidationUiState.Error(e.message ?: "Ocurrió un error al cargar los detalles")
            }
        }
    }

    // --- El resto de la lógica no cambia mucho, solo se adapta al nuevo estado ---
    fun requestAction(action: Action) {
        val currentState = _uiState.value
        if (currentState is ValidationUiState.Loaded) {
            _uiState.value = ValidationUiState.Confirming(currentState.txWithUser, action)
        }
    }

    fun cancelAction() {
        val currentState = _uiState.value
        if (currentState is ValidationUiState.Confirming) {
            _uiState.value = ValidationUiState.Loaded(currentState.txWithUser)
        }
    }

    fun performAction(action: Action) {
        val currentState = _uiState.value
        if (currentState !is ValidationUiState.Confirming) return

        viewModelScope.launch {
            val isApproved = (action == Action.APPROVE)

            // <<< CAMBIO CLAVE: Llamamos a la nueva función en FirebaseAuthManager >>>
            val result = FirebaseAuthManager.validateDeposit(
                txId = currentState.txWithUser.transaction.id,
                approve = isApproved
            )

            result.onSuccess {
                _uiState.value = ValidationUiState.Success
            }.onFailure { error ->
                _uiState.value = ValidationUiState.Error(error.message ?: "Error al validar la transacción")
            }
        }
    }
}

// --- Factory para el ViewModel (Actualizado) ---
class DepositValidationViewModelFactory(
    private val txId: String
    // <<< CAMBIO: Ya no necesitamos pasar el 'repo' >>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DepositValidationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DepositValidationViewModel(txId) as T // <<< CAMBIO
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}