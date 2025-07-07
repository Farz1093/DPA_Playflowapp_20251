package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.repository.TransactionRepository
import com.esan.payflowapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val txRepo: TransactionRepository,
    private val userRepo: UserRepository
) : ViewModel()  {

    private val _balance = MutableStateFlow(0L)
    val balance: StateFlow<Long> = _balance

    private val _recentTxs = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val recentTxs: StateFlow<List<TransactionEntity>> = _recentTxs

    init {
        val uid = FirebaseAuthManager.getCurrentUserUid()!!
        viewModelScope.launch {
            txRepo.getBalance(uid).collect { _balance.value = it }
        }
        viewModelScope.launch {
            txRepo.getRecentTransactions(uid).collect { _recentTxs.value = it }
        }
    }

}

class MainViewModelFactory(
    private val txRepo: TransactionRepository,
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(txRepo, userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}