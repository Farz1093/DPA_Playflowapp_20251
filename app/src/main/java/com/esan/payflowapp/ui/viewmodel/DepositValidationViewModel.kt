package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DepositValidationViewModel(
    private val txId: String,
    private val repo: TransactionRepository,
    private val fs: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ValidationUiState>(ValidationUiState.Loading)
    val uiState: StateFlow<ValidationUiState> = _uiState

    /** Carga la transacción desde Firestore en cuanto se crea el ViewModel */
    fun load() = viewModelScope.launch {
        _uiState.value = ValidationUiState.Loading
        try {
            val snap = fs.collection("transactions")
                .document(txId)
                .get()
                .await()
            val tx = snap.toObject(TransactionEntity::class.java)
                ?: throw IllegalStateException("Transacción no encontrada")
            _uiState.value = ValidationUiState.Loaded(tx)
        } catch (e: Exception) {
            _uiState.value = ValidationUiState.Error(e.localizedMessage ?: "Error desconocido")
        }
    }

    /** Pasa a estado de confirmación para mostrar diálogo */
    fun requestAction(action: Action) {
        val current = _uiState.value
        if (current is ValidationUiState.Loaded) {
            _uiState.value = ValidationUiState.Confirming(current.tx, action)
        }
    }

    /** Lanza la llamada al repositorio para completar o fallar la transacción */
    fun performAction(action: Action) = viewModelScope.launch {
        _uiState.value = ValidationUiState.Loading
        val result = repo.validateTransaction(txId, action == Action.APPROVE)
        if (result.isSuccess) {
            _uiState.value = ValidationUiState.Success
        } else {
            _uiState.value = ValidationUiState.Error(result.exceptionOrNull()?.localizedMessage
                ?: "No se pudo actualizar")
        }
    }
}

/**
 * Factory para crear el ViewModel con parámetros (txId y repo).
 */
class DepositValidationViewModelFactory(
    private val txId: String,
    private val repo: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DepositValidationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DepositValidationViewModel(txId, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}