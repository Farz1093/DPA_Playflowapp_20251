package com.esan.payflowapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.esan.payflowapp.ui.screens.DepositScreen
import com.esan.payflowapp.ui.screens.DepositValidationScreen
import com.esan.payflowapp.ui.screens.HomeScreen
import com.esan.payflowapp.ui.screens.LoginScreen
import com.esan.payflowapp.ui.screens.TransactionsHistoryScreen
import com.esan.payflowapp.ui.screens.TransactionsReportScreen
import com.esan.payflowapp.ui.screens.WithdrawScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("home") {
            DrawerScaffold(title = "Bienvenido", navController = navController) {
                HomeScreen()
            }
        }
        composable("deposit") {
            DrawerScaffold(title = "Depósito", navController = navController) {
                DepositScreen()
            }
        }
        //SOLO ADMIN
        composable("deposit-validation") {
            DrawerScaffold(title = "Validación de depósito", navController = navController) {
                DepositValidationScreen()
            }
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
    }
}