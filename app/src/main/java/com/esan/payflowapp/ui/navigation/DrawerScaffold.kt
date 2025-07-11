package com.esan.payflowapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val isAdmin by remember { mutableStateOf(SharedPreferencesManager.isAdmin(context)) }

    //ModalNavigationDrawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.padding(12.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    text = "Hola, $name"
                )
                Spacer(modifier = Modifier.padding(12.dp))
                HorizontalDivider()
                //Home navigation
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Menu"
                        )
                    },
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        navController.navigate("home")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Menu"
                        )
                    },
                    label = { Text("Depósito") },
                    selected = false,
                    onClick = {
                        navController.navigate("deposit")
                        scope.launch { drawerState.close() }
                    }
                )
                if (isAdmin) {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AccountBox,
                                contentDescription = "Menu"
                            )
                        },
                        label = { Text("Validación de depósito") },
                        selected = false,
                        onClick = {
                            navController.navigate("deposit-validation")
                            scope.launch { drawerState.close() }
                        }
                    )
                }
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    },
                    label = { Text("Transferencia") },
                    selected = false,
                    onClick = {
                        navController.navigate("transfer")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Menu"
                        )
                    },
                    label = { Text("Historial de Transacciones") },
                    selected = false,
                    onClick = {
                        navController.navigate("transactions-history")
                        scope.launch { drawerState.close() }
                    }
                )
                if (isAdmin) {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        label = { Text("Reporte de Transacciones") },
                        selected = false,
                        onClick = {
                            navController.navigate("transactions-report")
                            scope.launch { drawerState.close() }
                        }
                    )
                }
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ExitToApp,
                            contentDescription = "ExitToApp"
                        )
                    },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            FirebaseAuthManager.logoutUser()
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

            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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