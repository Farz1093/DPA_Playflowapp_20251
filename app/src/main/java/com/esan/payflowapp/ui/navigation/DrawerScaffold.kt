package com.esan.payflowapp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.esan.payflowapp.core.firebase.FirebaseAuthManager
import com.esan.payflowapp.core.pref.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScaffold(
    title: String = "Pay Flow",
    navController: NavController,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val name by remember { mutableStateOf(SharedPreferencesManager.getName(context)) }
    val accountNumber by remember { mutableStateOf(SharedPreferencesManager.getAccountNumber(context)) }
    val isAdmin by remember { mutableStateOf(SharedPreferencesManager.isAdmin(context)) }

    // --- Estado para el menú desplegable ---
    var reportsExpanded by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // --- 1. Cabecera con Información del Usuario ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Hola, $name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (!isAdmin) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cuenta: $accountNumber",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- 2. Acciones Principales ---
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        navController.navigate("home")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Depósito") },
                    label = { Text("Realizar Depósito") },
                    selected = false,
                    onClick = {
                        navController.navigate("deposit")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Transferencia") },
                    label = { Text("Realizar Transferencia") },
                    selected = false,
                    onClick = {
                        navController.navigate("transfer")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial de Transacciones") },
                    selected = false,
                    onClick = {
                        navController.navigate("transactions-history")
                        scope.launch { drawerState.close() }
                    }
                )

                // --- 3. Sección de Administración (solo para Admins) ---
                if (isAdmin) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "ADMINISTRACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = "Validación") },
                        label = { Text("Validación de Depósitos") },
                        selected = false,
                        onClick = {
                            navController.navigate("deposit-validation")
                            scope.launch { drawerState.close() }
                        }
                    )

                    // --- MENÚ DESPLEGABLE ---
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reportes") },
                        label = { Text("Reporte de Depósitos") },
                        // El ícono de la derecha cambia si está expandido o no
                        badge = {
                            Icon(
                                imageVector = if (reportsExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir/Contraer"
                            )
                        },
                        selected = false,
                        onClick = { reportsExpanded = !reportsExpanded } // La acción es solo expandir
                    )


                    AnimatedVisibility(visible = reportsExpanded) {
                        Column {
                            NavigationDrawerItem(
                                label = { Text("Resumen de Estados") },
                                selected = false,
                                onClick = {
                                    // TODO: Reemplaza con la ruta de tu nuevo reporte
                                    navController.navigate("transactions-report")
                                    scope.launch { drawerState.close() }
                                },
                                // El padding y un color diferente lo hacen ver como un sub-item
                                modifier = Modifier.padding(start = 16.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                            NavigationDrawerItem(
                                label = { Text("Informe") },
                                selected = false,
                                onClick = {

                                    navController.navigate("transactions-report-advanced")
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(start = 16.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }


                Spacer(Modifier.weight(1f))


                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión") },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            FirebaseAuthManager.logoutUser(context)
                            drawerState.close()
                            withContext(Dispatchers.Main) {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        }
    }
}