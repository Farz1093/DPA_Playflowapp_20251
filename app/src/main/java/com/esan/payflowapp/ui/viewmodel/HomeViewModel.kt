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
import com.esan.payflowapp.ui.model.GeneralState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private var _state = MutableLiveData<GeneralState>(GeneralState.Idle)
    val state: LiveData<GeneralState> get() = _state

    private var _trxList = MutableLiveData<List<Transaction>>(emptyList())
    val trxList: LiveData<List<Transaction>> get() = _trxList

    private var _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

    init {
        loadList()
    }

    fun getUserData(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val user = FirebaseAuthManager.getUserData()
        SharedPreferencesManager.saveName(context, user.name)
        SharedPreferencesManager.saveAccountNumber(context, user.accountNumber)
        SharedPreferencesManager.saveBalance(context, user.balance)
        SharedPreferencesManager.saveIsAdmin(context, user.isAdmin)

        _balance.postValue(user.balance)
    }

    private fun loadList() = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            _state.postValue(GeneralState.Loading)
            val list = FirebaseAuthManager.getLastTrx()
            Log.e("WAA", "list=${list.size}")
            _trxList.postValue(FirebaseAuthManager.getLastTrx())
            _state.postValue(GeneralState.Idle)
        }.onFailure {
            _state.postValue(GeneralState.Fail(message = it.localizedMessage.orEmpty()))
            it.printStackTrace()
        }
    }

}


class HomeViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
