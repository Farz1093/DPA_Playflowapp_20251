package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.esan.payflowapp.core.firebase.models.TransactionWithUser
import com.esan.payflowapp.data.repository.TransactionRepository
import com.esan.payflowapp.ui.viewmodel.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Colores consistentes
private val DetailLabelColor = Color(0xFF6B7280)
private val DetailValueColor = Color(0xFF111827)
private val DetailCardBackground = Color.White
private val ScreenBackground = Color(0xFFF9FAFB)
private val RejectButtonColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositValidationDetailScreen(
    txId: String,
    repo: TransactionRepository,
    navController: NavController
) {
    val vm: DepositValidationViewModel = viewModel(
        factory = DepositValidationViewModelFactory(txId, repo)
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(txId) {
        vm.load()
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("Validar Depósito") },
                // <<< CAMBIO: Botón para regresar >>>
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBackground
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                ValidationUiState.Loading -> CircularProgressIndicator()
                is ValidationUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ValidationUiState.Loaded -> {

                }
                is ValidationUiState.Confirming -> {
                    // El diálogo se muestra sobre el contenido anterior


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
    txWithUser: TransactionWithUser,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val tx = txWithUser.transaction
    var showReceiptDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Espacio para los botones fijos de abajo
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de Información del Usuario
            item {
                InfoCard {
                    DetailItem(
                        icon = Icons.Default.Person,
                        label = "Usuario",
                        value = txWithUser.userName
                    )
                    Divider(color = ScreenBackground)
                    DetailItem(
                        icon = Icons.Default.Fingerprint,
                        label = "ID de Usuario",
                        value = tx.userId,
                        isMonospace = true
                    )
                }
            }

            // Card de Información de la Transacción
            item {
                val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }
                val formattedDate = remember(tx.createdAt) {
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
                }
                InfoCard {
                    DetailItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Monto",
                        value = currencyFormatter.format(tx.amount),
                        valueFontSize = 20.sp,
                        valueWeight = FontWeight.Bold
                    )
                    Divider(color = ScreenBackground)
                    DetailItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Fecha de Creación",
                        value = formattedDate
                    )
                    Divider(color = ScreenBackground)
                    DetailItem(
                        icon = Icons.Default.QrCode,
                        label = "ID de Transacción",
                        value = tx.id,
                        isMonospace = true
                    )
                }
            }

            // Card del Comprobante
            tx.receiptUrl?.let { url ->
                item {
                    ReceiptCard(
                        url = url,
                        onClick = { showReceiptDialog = true }
                    )
                }
            }
        }

        // Botones fijos en la parte inferior
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ScreenBackground)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onApprove,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) { Text("Aprobar") }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onReject,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RejectButtonColor)
            ) { Text("Rechazar") }
        }
    }

    if (showReceiptDialog && tx.receiptUrl != null) {
        ReceiptDialog(url = tx.receiptUrl, onDismiss = { showReceiptDialog = false })
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DetailCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    valueWeight: FontWeight = FontWeight.Normal,
    isMonospace: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DetailLabelColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = DetailLabelColor)
            Text(
                value,
                fontSize = valueFontSize,
                color = DetailValueColor,
                fontWeight = valueWeight,
                fontFamily = if (isMonospace) androidx.compose.ui.text.font.FontFamily.Monospace else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReceiptCard(url: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Comprobante de Pago", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Comprobante",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
private fun ReceiptDialog(url: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConfirmDialog(
    txWithUser: TransactionWithUser,
    action: Action,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }
    AlertDialog(
        onDismissRequest = onCancel,
        title   = { Text(if (action == Action.APPROVE) "Confirmar Aprobación" else "Confirmar Rechazo") },
        text    = {
            Text("¿Deseas ${action.name.lowercase()} el depósito de ${currencyFormatter.format(txWithUser.transaction.amount)} de ${txWithUser.userName}?")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
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