package com.esan.payflowapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.notifications.AdminNotificationsManager
import com.esan.payflowapp.core.notifications.UserNotificationsManager
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import com.esan.payflowapp.ui.model.GeneralState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    private var _state = MutableLiveData<GeneralState>(GeneralState.Loading)
    val state: LiveData<GeneralState> get() = _state

    init {
        validateLogin()
    }

    private fun validateLogin() {
        _state.value = GeneralState.Loading
        if (FirebaseAuthManager.getCurrentUserUid().isNotEmpty()) {
            _state.value = GeneralState.Success
        } else {
            _state.value = GeneralState.Idle
        }
    }

    fun doLogin(context: Context, email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        _state.postValue(GeneralState.Loading)
        try {
            val result = FirebaseAuthManager.loginUser(email, password)
            if (result.isSuccess) {
                val user = FirebaseAuthManager.getUserData()
                SharedPreferencesManager.saveName(context, user.name)
                SharedPreferencesManager.saveAccountNumber(context, user.accountNumber)
                SharedPreferencesManager.saveBalance(context, user.balance)
                SharedPreferencesManager.saveIsAdmin(context, user.isAdmin)

                if (user.isAdmin) {
                    // Si es admin, inicia el listener para nuevos depósitos pendientes.
                    AdminNotificationsManager.startListening(context)
                } else {
                    // Si es un usuario normal, inicia el listener para el estado de SUS depósitos.
                    UserNotificationsManager.startListeningForDepositUpdates(context)
                }

                _state.postValue(GeneralState.Success)
            } else {
                _state.postValue(GeneralState.Fail(result.exceptionOrNull()?.localizedMessage.orEmpty()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.postValue(GeneralState.Fail(e.localizedMessage.orEmpty()))
        }
    }

}


class LoginViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
