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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.esan.payflowapp.core.firebase.model.TransactionWithUser
import com.esan.payflowapp.ui.viewmodel.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// --- Colores (sin cambios) ---
private val DetailLabelColor = Color(0xFF6B7280)
private val DetailValueColor = Color(0xFF111827)
private val DetailCardBackground = Color.White
private val ScreenBackground = Color(0xFFF9FAFB)
private val RejectButtonColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositValidationDetailScreen(
    txId: String,
    navController: NavController
) {
    // <<< CAMBIO: Se usa el nuevo ViewModel que obtiene los datos combinados >>>
    val vm: DepositValidationViewModel = viewModel(
        factory = DepositValidationViewModelFactory(txId)
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
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ValidationUiState.Loading -> LoadingAnimation()
                is ValidationUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is ValidationUiState.Loaded -> {
                    ValidationContent(
                        txWithUser = state.txWithUser,
                        onApprove = { vm.requestAction(Action.APPROVE) },
                        onReject = { vm.requestAction(Action.REJECT) }
                    )
                }
                is ValidationUiState.Confirming -> {
                    ValidationContent(
                        txWithUser = state.txWithUser,
                        onApprove = {},
                        onReject = {}
                    )
                    ConfirmDialog(
                        txWithUser = state.txWithUser,
                        action = state.action,
                        onConfirm = { vm.performAction(state.action) },
                        onCancel = { vm.cancelAction() } // Usamos el nuevo método para cancelar
                    )
                }
                is ValidationUiState.Success -> {
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
    // <<< CAMBIO: 'tx' ahora es de tipo DepositTransaction >>>
    val tx = txWithUser.transaction
    var showReceiptDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        // <<< CAMBIO CLAVE: Se usa 'uid' en lugar de 'userId' >>>
                        value = tx.uid,
                        isMonospace = true
                    )
                }
            }

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

            /*tx.receiptUrl?.let { url ->
                item {
                    ReceiptCard(
                        url = url,
                        onClick = { showReceiptDialog = true }
                    )
                }
            }*/
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ScreenBackground)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f).height(52.dp),
                onClick = onApprove,
                shape = RoundedCornerShape(12.dp),
            ) { Text("Aprobar") }

            Button(
                modifier = Modifier.weight(1f).height(52.dp),
                onClick = onReject,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RejectButtonColor)
            ) { Text("Rechazar") }
        }
    }

    /*if (showReceiptDialog && tx.receiptUrl != null) {
        ReceiptDialog(url = tx.receiptUrl, onDismiss = { showReceiptDialog = false })
    }*/
}

// --- El resto de componentes de UI (InfoCard, DetailItem, etc.) no necesitan cambios ---
// Los pego para que el archivo esté completo.

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
    icon: ImageVector,
    label: String,
    value: String,
    valueFontSize: TextUnit = 16.sp,
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
                text = value,
                fontSize = valueFontSize,
                color = DetailValueColor,
                fontWeight = valueWeight,
                fontFamily = if (isMonospace) FontFamily.Monospace else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReceiptCard(url: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Comprobante de Pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
    val actionText = if (action == Action.APPROVE) "aprobar" else "rechazar"
    AlertDialog(
        onDismissRequest = onCancel,
        title   = { Text("Confirmar ${actionText.replaceFirstChar { it.titlecase(Locale.ROOT) }}") },
        text    = {
            Text("¿Deseas $actionText el depósito de ${currencyFormatter.format(txWithUser.transaction.amount)} de ${txWithUser.userName}?")
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

    if (progress == 1f) {
        LaunchedEffect(Unit) {
            delay(1000)
            onEnd()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LottieAnimation(
            composition = composition,
            progress    = { progress },
            modifier    = Modifier.size(200.dp)
        )
    }
}

@Composable
private fun LoadingAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets6.lottiefiles.com/packages/lf20_rZ4c2a.json")
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(250.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Cargando detalles...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}