package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.repository.TransactionRepository
import com.esan.payflowapp.ui.viewmodel.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.window.Dialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositValidationDetailScreen(
    txId: String,
    repo: TransactionRepository,
    navController: NavController
) {
    // 1️⃣ ViewModel
    val vm: DepositValidationViewModel = viewModel(
        factory = DepositValidationViewModelFactory(txId, repo)
    )
    val uiState by vm.uiState.collectAsState()

    // 2️⃣ Estado del host para Snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    // 3️⃣ Haptics
    val haptic = LocalHapticFeedback.current

    // 4️⃣ Carga inicial
    LaunchedEffect(txId) {
        vm.load()
    }

    // 5️⃣ Mostrar Snackbar en Error o Success
    LaunchedEffect(uiState) {
        when (uiState) {
            is ValidationUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ValidationUiState.Error).message)
            }
            ValidationUiState.Success -> {
                snackbarHostState.showSnackbar("¡Operación completada!")
            }
            else -> { /* nada */ }
        }
    }

    // 6️⃣ Estructura Scaffold
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Validar Depósito") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                ValidationUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ValidationUiState.Error -> {
                    Text(
                        text = (uiState as ValidationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is ValidationUiState.Loaded -> {
                    ValidationContent(
                        tx = (uiState as ValidationUiState.Loaded).tx,
                        onApprove = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.requestAction(Action.APPROVE)
                        },
                        onReject = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.requestAction(Action.REJECT)
                        }
                    )
                }
                is ValidationUiState.Confirming -> {
                    ConfirmDialog(
                        tx     = (uiState as ValidationUiState.Confirming).tx,
                        action = (uiState as ValidationUiState.Confirming).action,
                        onConfirm = {
                            vm.performAction((uiState as ValidationUiState.Confirming).action)
                        },
                        onCancel = { vm.load() }
                    )
                }
                ValidationUiState.Success -> {
                    SuccessAnimation {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationContent(
    tx: TransactionEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showReceipt by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Usuario
        Text("Usuario", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Text(tx.userId, fontWeight = FontWeight.Bold)

        // Monto
        Text("Monto", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Text("S/ ${tx.amount}", fontWeight = FontWeight.Bold)

        // Fecha
        Text("Fecha", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        val formattedDate = remember(tx.createdAt) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(tx.createdAt))
        }

        Text("Fecha", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Text(formattedDate, fontWeight = FontWeight.Bold)

        // Comprobante
        tx.receiptUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Comprobante",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showReceipt = true }
            )
        }

        // Botones
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onApprove
            ) { Text("Aprobar") }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Rechazar") }
        }
    }

    // Dialogo full screen para comprobante con zoom
    if (showReceipt && tx.receiptUrl != null) {
        Dialog(onDismissRequest = { showReceipt = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            // Implementa zoom/pan si deseas
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = tx.receiptUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    tx: TransactionEntity,
    action: Action,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title   = {
            Text(
                text = if (action == Action.APPROVE)
                    "Confirmar aprobación" else "Confirmar rechazo"
            )
        },
        text    = {
            Text("¿Deseas ${action.name.lowercase()} el depósito de S/ ${tx.amount}?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (action == Action.APPROVE) "Sí, aprobar" else "Sí, rechazar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SuccessAnimation(onEnd: () -> Unit) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_xdfeea13.json")
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        speed = 1.5f,
        restartOnPlay = false
    )
    // Animación centrada
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LottieAnimation(
            composition = composition,
            progress    = progress,
            modifier    = Modifier.size(200.dp)
        )
    }
    if (progress == 1f) {
        LaunchedEffect(Unit) { onEnd() }
    }
}