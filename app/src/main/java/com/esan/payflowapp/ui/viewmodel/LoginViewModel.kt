package com.esan.payflowapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.esan.payflowapp.core.notifications.AdminNotificationsManager

class LoginViewModel : ViewModel() {

    private var _state = MutableLiveData<LoginState>(LoginState.Loading)
    val state: LiveData<LoginState> get() = _state

    init {
        validateLogin()
    }

    private fun validateLogin() {
        _state.value = LoginState.Loading
        if (FirebaseAuthManager.getCurrentUserUid() != null) {
            _state.value = LoginState.Success
        } else {
            _state.value = LoginState.Idle
        }
    }

    fun doLogin(context: Context, email: String, password: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(LoginState.Loading)
            try {
                val result = FirebaseAuthManager.loginUser(email, password)
                if (result.isSuccess) {
                    // 1) Obtengo perfil y guardo en prefs
                    val profile = FirebaseAuthManager.getUserProfile()
                    SharedPreferencesManager.saveName(context, profile.name)
                    SharedPreferencesManager.saveIsAdmin(context, profile.role == "ADMIN")
                    SharedPreferencesManager.saveEmail(context, profile.email)
                    SharedPreferencesManager.saveCreatedAt(context, profile.createdAt)

                    if (profile.role == "ADMIN") {
                        AdminNotificationsManager.startListening(context)
                    }

                    // 2) Disparo un sync inmediato
                    val constraints =   Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()


                    // 3) Aviso a la UI que todo fue ok
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
