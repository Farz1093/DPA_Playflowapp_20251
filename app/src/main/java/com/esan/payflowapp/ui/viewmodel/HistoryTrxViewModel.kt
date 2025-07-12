package com.esan.payflowapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.firebase.model.Transaction
import com.esan.payflowapp.ui.model.GeneralState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryTrxViewModel : ViewModel() {

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val endOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private val weekAgo = todayStart - 6 * 24 * 60 * 60 * 1000L

    private var _trxList = MutableLiveData<List<Transaction>>(emptyList())
    val trxList: LiveData<List<Transaction>> get() = _trxList

    private var _state = MutableLiveData<GeneralState>(GeneralState.Idle)
    val state: LiveData<GeneralState> get() = _state

    fun getHistory(
        filter: RangeFilter,
        customFrom: Long?,
        customTo: Long?
    ) = viewModelScope.launch(Dispatchers.IO) {
        _state.postValue(GeneralState.Loading)

        val from = when (filter) {
            RangeFilter.TODAY -> todayStart
            RangeFilter.WEEK -> weekAgo
            RangeFilter.RANGE -> customFrom ?: todayStart
        }
        val to = when (filter) {
            RangeFilter.TODAY, RangeFilter.WEEK -> endOfToday
            RangeFilter.RANGE -> customTo ?: endOfToday
        }

        runCatching {
            FirebaseAuthManager.getTransactionHistory(from = from, to = to)
        }.onSuccess {
            _trxList.postValue(it)
            _state.postValue(GeneralState.Idle)
        }.onFailure {
            it.printStackTrace()
            _state.postValue(GeneralState.Fail(message = it.message ?: it.localizedMessage))
        }
    }

}

enum class RangeFilter { TODAY, WEEK, RANGE }

class HistoryTrxViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryTrxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryTrxViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
