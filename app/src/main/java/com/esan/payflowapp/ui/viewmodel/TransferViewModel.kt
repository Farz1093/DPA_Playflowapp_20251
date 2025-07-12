package com.esan.payflowapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import com.esan.payflowapp.ui.model.GeneralState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferViewModel : ViewModel() {

    private var _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

    private var _state = MutableLiveData<GeneralState>(GeneralState.Idle)
    val state: LiveData<GeneralState> get() = _state

    fun getData(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val user = FirebaseAuthManager.getUserData()
            SharedPreferencesManager.saveName(context, user.name)
            SharedPreferencesManager.saveAccountNumber(context, user.accountNumber)
            SharedPreferencesManager.saveBalance(context, user.balance)
            SharedPreferencesManager.saveIsAdmin(context, user.isAdmin)

            _balance.postValue(user.balance)
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun transferMoney(
        destinyAccount: String,
        amount: Double
    ) = viewModelScope.launch(Dispatchers.IO) {
        _state.postValue(GeneralState.Loading)
        runCatching {
            FirebaseAuthManager.transferMoney(destinyAccount, amount)
        }.onSuccess {
            _state.postValue(GeneralState.Success)
        }.onFailure {
            it.printStackTrace()
            _state.postValue(GeneralState.Fail(message = it.message ?: it.localizedMessage))
        }
    }

}

class TransferViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransferViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
