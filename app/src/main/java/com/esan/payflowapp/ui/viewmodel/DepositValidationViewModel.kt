package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.models.TransactionWithUser
import com.esan.payflowapp.core.firebase.models.UserProfile
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asStateFlow

class DepositValidationViewModel(
    private val txId: String,
    private val repo: TransactionRepository,
    private val fs: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ValidationUiState>(ValidationUiState.Loading)
    val uiState: StateFlow<ValidationUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.value = ValidationUiState.Loading
        try {
            // Paso 1: Obtener la transacción
            val txSnap = fs.collection("transactions").document(txId).get().await()
            val tx = txSnap.toObject(TransactionEntity::class.java)
                ?: throw IllegalStateException("Transacción no encontrada con ID: $txId")

            // Paso 2: Obtener el nombre del usuario
            // (Usamos un simple .get() aquí, 'async' no es estrictamente necesario pero está bien)
            val userSnap = fs.collection("users").document(tx.userId).get().await()
            val user = userSnap.toObject(UserProfile::class.java)
            val userName = user?.name ?: "Usuario Desconocido"

            // <<< ¡AQUÍ ESTÁ LA CORRECCIÓN! DESCOMENTAMOS LA LÍNEA >>>
            // Paso 3: Combinar y actualizar el estado
            _uiState.value = ValidationUiState.Loaded(TransactionWithUser(tx, userName))

        } catch (e: Exception) {
            _uiState.value = ValidationUiState.Error(e.localizedMessage ?: "Error desconocido")
        }
    }

    /** Pasa a estado de confirmación para mostrar diálogo */
    fun requestAction(action: Action) {
        val current = _uiState.value
        if (current is ValidationUiState.Loaded) {
            _uiState.value = ValidationUiState.Confirming(current.txWithUser, action)
        }
    }

    /** Vuelve al estado cargado si el usuario cancela la confirmación */
    fun cancelAction() {
        val current = _uiState.value
        if (current is ValidationUiState.Confirming) {
            _uiState.value = ValidationUiState.Loaded(current.txWithUser)
        }
    }

    /** Lanza la llamada al repositorio para completar o fallar la transacción */
    fun performAction(action: Action) {
        val current = _uiState.value
        if (current !is ValidationUiState.Confirming) return

        viewModelScope.launch {
            _uiState.value = ValidationUiState.Loading
            val result = repo.validateTransaction(current.txWithUser.transaction.id, action == Action.APPROVE)


            if (result.isSuccess) {
                _uiState.value = ValidationUiState.Success
            } else {

                val errorMessage = result.exceptionOrNull()?.localizedMessage ?: "No se pudo actualizar"
                _uiState.value = ValidationUiState.Error(errorMessage)
            }
        }
    }
}



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