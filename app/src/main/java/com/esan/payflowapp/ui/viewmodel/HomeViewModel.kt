package com.esan.payflowapp.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private var _trxList = MutableLiveData<List<String>>(emptyList())
    val trxList: LiveData<List<String>> get() = _trxList

    private var _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

    init {
        loadList()
    }

    fun getUserData(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val triple = FirebaseAuthManager.getUserData()
        SharedPreferencesManager.saveName(context, triple.first)
        SharedPreferencesManager.saveBalance(context, triple.second)
        SharedPreferencesManager.saveIsAdmin(context, triple.third)

        _balance.postValue(triple.second)
    }

    private fun loadList() {
        //TO BE CONTINUE
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
