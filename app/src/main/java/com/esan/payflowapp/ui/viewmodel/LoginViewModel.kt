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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    private var _state = MutableLiveData<LoginState>(LoginState.Loading)
    val state: LiveData<LoginState> get() = _state

    init {
        validateLogin()
    }

    private fun validateLogin() {
        _state.value = LoginState.Loading
        if (FirebaseAuthManager.getCurrentUserUid().isNotEmpty()) {
            _state.value = LoginState.Success
        } else {
            _state.value = LoginState.Idle
        }
    }

    fun doLogin(context: Context, email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {


        _state.postValue(LoginState.Loading)

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

                _state.postValue(LoginState.Success)
            } else {
                _state.postValue(LoginState.Fail(result.exceptionOrNull()?.localizedMessage.orEmpty()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.postValue(LoginState.Fail(e.localizedMessage.orEmpty()))
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        class Fail(val message: String) : LoginState()
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
