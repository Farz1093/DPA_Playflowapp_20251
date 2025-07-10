package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esan.payflowapp.data.repository.UserRepository
import com.esan.payflowapp.ui.viewmodel.AdminUsersViewModel
import com.esan.payflowapp.ui.viewmodel.AdminUsersViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState

@Composable
fun AdminUsersScreen(
    repo: UserRepository = remember { UserRepository() },
    vm: AdminUsersViewModel = viewModel(
        factory = AdminUsersViewModelFactory(repo)
    )
) {
    // 1. Estado del ViewModel
    val error      by vm.error.collectAsState()
    val successMsg by vm.successMessage.collectAsState()

    // 2. SnackbarHost para mostrar el mensaje bonito
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. Cuando llega un successMsg, lanzamos el Snackbar y lo limpiamos


    // 4. Campos del formulario
    var email by remember { mutableStateOf("") }
    var pw    by remember { mutableStateOf("") }
    var name  by remember { mutableStateOf("") }
    var role  by remember { mutableStateOf("USER") }

    LaunchedEffect(successMsg) {
        successMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.clearSuccessMessage()
            email = ""
            pw    = ""
            name  = ""
            role  = "USER"
        }
    }

    // 5. Scope para lanzar la petición de registro
    val scope = rememberCoroutineScope()

    // 6. Scaffold con SnackbarHost
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            // Aquí podrías meter tu DropdownMenu para role si lo deseas:
            // Text("Role: $role")

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        vm.register(email, pw, name, role)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Usuario")
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}
