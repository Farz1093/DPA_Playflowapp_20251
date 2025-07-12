package com.esan.payflowapp.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar


data class UserReportInfo(
    val uid: String = "",
    val name: String = "Usuario Desconocido",
    val totalDeposited: Double = 0.0,
    val totalSpent: Double = 0.0,
    val depositCount: Int = 0,
    val transactionCount: Int = 0,
    val lastActivityTimestamp: Long = 0L
)


data class AdvancedReportData(
    val totalDepositedInPeriod: Double = 0.0,
    val totalSpentByDepositorsInPeriod: Double = 0.0,
    val topDepositors: List<UserReportInfo> = emptyList(),
    val mostActiveUsers: List<UserReportInfo> = emptyList(),
    val dormantWhales: List<UserReportInfo> = emptyList()
)

sealed class AdvancedReportUiState {
    object Loading : AdvancedReportUiState()
    data class Success(val data: AdvancedReportData) : AdvancedReportUiState()
    data class Error(val message: String) : AdvancedReportUiState()
}




class AdvancedDepositReportViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<AdvancedReportUiState>(AdvancedReportUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        generateAdvancedReport()
    }

    private fun generateAdvancedReport() {
        viewModelScope.launch {
            _uiState.value = AdvancedReportUiState.Loading
            try {

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -90)
                val periodStartTimestamp = calendar.timeInMillis

                val allCompletedDepositsSnapshot = firestore.collection("deposit_data")
                    .whereEqualTo("status", "COMPLETED")
                    .get().await()

                val depositsInPeriod = allCompletedDepositsSnapshot.documents.filter {
                    val timestamp = it.getLong("createdAt")
                    timestamp != null && timestamp >= periodStartTimestamp
                }

                if (depositsInPeriod.isEmpty()) {
                    _uiState.value = AdvancedReportUiState.Success(AdvancedReportData())
                    return@launch
                }

                val depositorIds = depositsInPeriod.mapNotNull { it.getString("uid") }.distinct()

                if (depositorIds.isEmpty()) {
                    _uiState.value = AdvancedReportUiState.Success(AdvancedReportData())
                    return@launch
                }

                val usersSnapshot = firestore.collection("user_data")
                    .whereIn(FieldPath.documentId(), depositorIds)
                    .get().await()

                val allSpendingSnapshot = firestore.collection("trx_data")
                    .whereIn("from_uid", depositorIds)
                    .get().await()


                // <<< PRIMER CAMBIO: Leer el campo 'date' como un Timestamp, no como un Long >>>
                val spendingInPeriod = allSpendingSnapshot.documents.filter {
                    // 1. Obtiene el objeto Timestamp
                    val timestampObject = it.getTimestamp("date")
                    // 2. Convierte el objeto a milisegundos y compara
                    val timestampMillis = timestampObject?.toDate()?.time
                    timestampMillis != null && timestampMillis >= periodStartTimestamp
                }


                // --- 4. Procesar y agregar los datos ---
                val userReportMap = mutableMapOf<String, UserReportInfo>()

                usersSnapshot.documents.forEach { doc ->
                    userReportMap[doc.id] = UserReportInfo(uid = doc.id, name = doc.getString("name") ?: "N/A")
                }

                var totalDepositedInPeriod = 0.0
                depositsInPeriod.forEach { doc ->
                    val uid = doc.getString("uid") ?: return@forEach
                    val amount = doc.getDouble("amount") ?: 0.0
                    totalDepositedInPeriod += amount
                    val info = userReportMap[uid] ?: UserReportInfo(uid = uid)
                    userReportMap[uid] = info.copy(
                        totalDeposited = info.totalDeposited + amount,
                        depositCount = info.depositCount + 1
                    )
                }

                var totalSpentByDepositors = 0.0
                spendingInPeriod.forEach { doc ->
                    val uid = doc.getString("from_uid") ?: return@forEach
                    val amount = doc.getDouble("amount") ?: 0.0
                    totalSpentByDepositors += amount

                    // <<< SEGUNDO CAMBIO: Hacemos lo mismo aquí >>>
                    val timestamp = doc.getTimestamp("date")?.toDate()?.time ?: 0L

                    val info = userReportMap[uid] ?: UserReportInfo(uid = uid)
                    userReportMap[uid] = info.copy(
                        totalSpent = info.totalSpent + amount,
                        transactionCount = info.transactionCount + 1,
                        lastActivityTimestamp = maxOf(info.lastActivityTimestamp, timestamp)
                    )
                }

                val allUsers = userReportMap.values.toList()

                val topDepositors = allUsers.sortedByDescending { it.totalDeposited }.take(5)
                val mostActiveUsers = allUsers.sortedByDescending { it.totalSpent * it.transactionCount }.take(5)
                val thirtyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.timeInMillis
                val dormantWhales = allUsers.filter {
                    it.totalDeposited > 1000 && it.transactionCount > 0 && it.lastActivityTimestamp < thirtyDaysAgo
                }.sortedByDescending { it.totalDeposited }.take(5)

                val finalReport = AdvancedReportData(
                    totalDepositedInPeriod = totalDepositedInPeriod,
                    totalSpentByDepositorsInPeriod = totalSpentByDepositors,
                    topDepositors = topDepositors,
                    mostActiveUsers = mostActiveUsers,
                    dormantWhales = dormantWhales
                )
                _uiState.value = AdvancedReportUiState.Success(finalReport)

            } catch (e: Exception) {
                _uiState.value = AdvancedReportUiState.Error(e.message ?: "Error al procesar el reporte.")
            }
        }
    }
}