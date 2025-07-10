package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.models.UserProfile
import com.esan.payflowapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val repo: UserRepository
): ViewModel() {

    // 1. Reemplaza MutableLiveData por MutableStateFlow
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {

        viewModelScope.launch {

        }
    }

    /** Llama al repo para registrar un nuevo usuario */
    fun register(email: String, password: String, name: String, role: String) {
        viewModelScope.launch {
            _error.value = null
            _successMessage.value = null
            val result = repo.register(email, password, name, role)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.localizedMessage
            } else {
                _successMessage.value = "🎉 Usuario “$name” agregado correctamente"
            }
        }
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

@Suppress("UNCHECKED_CAST")
class AdminUsersViewModelFactory(
    private val repo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUsersViewModel::class.java)) {
            return AdminUsersViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}