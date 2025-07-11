package com.esan.payflowapp.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.firebase.model.Transaction
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import com.esan.payflowapp.ui.viewmodel.LoginViewModel.LoginState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DepositViewModel : ViewModel() {

    private var _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

    private var _state = MutableLiveData<DepositState>(DepositState.Loading)
    val state: LiveData<DepositState> get() = _state

    fun getData(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val triple = FirebaseAuthManager.getUserData()
            SharedPreferencesManager.saveName(context, triple.first)
            SharedPreferencesManager.saveBalance(context, triple.second)
            SharedPreferencesManager.saveIsAdmin(context, triple.third)

            _balance.postValue(triple.second)
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun createDeposit(
        destinyAccount: String,
        amount: Double
    ) = viewModelScope.launch(Dispatchers.IO) {
        _state.postValue(DepositState.Loading)
        runCatching {
            FirebaseAuthManager.createDeposit(destinyAccount, amount)
        }.onSuccess {
            _state.postValue(DepositState.Success)
        }.onFailure {
            it.printStackTrace()
            _state.postValue(DepositState.Fail(message = it.message ?: it.localizedMessage))
        }
    }

    sealed class DepositState {
        object Idle : DepositState()
        object Loading : DepositState()
        object Success : DepositState()
        class Fail(val message: String) : DepositState()
    }

}

class DepositViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DepositViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DepositViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
