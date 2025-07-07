package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DepositViewModel(
    private val repo: TransactionRepository
) : ViewModel() {

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onAmountChange(value: String) {
        _amount.value = value
    }

    fun submitDeposit() {
        val amt = _amount.value.toLongOrNull()
        if (amt == null || amt <= 0L) {
            _error.value = "Ingresa un monto válido"
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val uid = com.esan.payflowapp.core.firebase.FirebaseAuthManager
                    .getCurrentUserUid()!!
                val tx = TransactionEntity(
                    id         = UUID.randomUUID().toString(),
                    userId     = uid,
                    type       = "DEPOSIT",
                    amount     = amt,
                    status     = "PENDING",
                    createdAt  = System.currentTimeMillis()
                )
                repo.deposit(tx)
                _error.value = null
            } catch(e:Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

}

class DepositViewModelFactory(
    private val repo: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DepositViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DepositViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown VM")
    }
}