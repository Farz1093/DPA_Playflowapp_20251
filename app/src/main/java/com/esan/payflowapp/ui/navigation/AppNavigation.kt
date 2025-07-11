package com.esan.payflowapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import com.esan.payflowapp.ui.screens.AdminUsersScreen
import com.esan.payflowapp.ui.screens.DepositScreen
import com.esan.payflowapp.ui.screens.DepositValidationScreen
import com.esan.payflowapp.ui.screens.HomeScreen
import com.esan.payflowapp.ui.screens.LoginScreen
import com.esan.payflowapp.ui.screens.TransactionsHistoryScreen
import com.esan.payflowapp.ui.screens.TransactionsReportScreen
import com.esan.payflowapp.ui.screens.WithdrawScreen
import androidx.compose.ui.platform.LocalContext
import com.esan.payflowapp.PayFlowApplication
import com.esan.payflowapp.data.repository.TransactionRepository
import com.esan.payflowapp.ui.screens.DepositValidationDetailScreen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context     = LocalContext.current
    val isAdmin     = SharedPreferencesManager.isAdmin(context)

    // Creamos un repo base que reusaremos en todas las pantallas
    val txRepo = TransactionRepository(
        firestore = FirebaseFirestore.getInstance()
    )

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("home") {
            DrawerScaffold(title = "Bienvenido", navController = navController) {
                HomeScreen()
            }
        }
        composable("deposit") {
            DrawerScaffold(title = "Depósito", navController = navController) {
                DepositScreen(
                    navBack = { navController.popBackStack() }  // ← aquí
                )
            }
        }
        // VALIDACIÓN (ADMIN) → LISTA DE PENDIENTES
        composable("deposit-validation") {
            DrawerScaffold(title = "Depósitos Pendientes", navController = navController) {
                // Esta pantalla lista todos los pendientes y navega al detalle
                DepositValidationScreen(
                    navController = navController
                )
            }
        }

        composable("deposit-validation/{id}") { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("id") ?: ""
            DepositValidationDetailScreen(txId =  txId,repo  = txRepo,navController = navController, )
        }

        composable("withdraw") {
            DrawerScaffold(title = "Retiro", navController = navController) {
                WithdrawScreen()
            }
        }
        composable("transactions-history") {
            DrawerScaffold(title = "Historial de Transacciones", navController = navController) {
                TransactionsHistoryScreen()
            }
        }
        //SOLO ADMIN
        composable("transactions-report") {
            DrawerScaffold(title = "Reporte de Transacciones", navController = navController) {
                TransactionsReportScreen()
            }
        }

        if (isAdmin) {
            composable("admin/users") {
                DrawerScaffold(title = "Usuarios", navController) {
                    AdminUsersScreen()
                }
            }
        }
    }
}