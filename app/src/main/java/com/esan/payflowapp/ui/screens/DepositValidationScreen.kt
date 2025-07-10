package com.esan.payflowapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

// --- Definiciones de colores para que sea más fácil de mantener ---
val LightGreyBackground = Color(0xFFF5F7FA)
val GradientStart = Color(0xFF6A82FB)
val GradientEnd = Color(0xFF7A6AF8)
val AmountColor = Color(0xFF2E7D32) // Un verde oscuro para el dinero
val PendingStatusBg = Color(0xFFFFF0C1)
val PendingStatusText = Color(0xFF8C5B00)
val IconBgColor = Color(0xFFE3F2FD) // Un azul muy claro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositValidationScreen(
    navController: NavController,
    repo: TransactionRepository
) {
    // 1️⃣ Estado y escucha Firestore (sin cambios en la lógica)
    var pendingList by remember { mutableStateOf(emptyList<TransactionEntity>()) }
    val firestore = FirebaseFirestore.getInstance()
    LaunchedEffect(Unit) {
        firestore.collection("transactions")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snaps, _ ->
                pendingList = snaps?.toObjects(TransactionEntity::class.java)?.sortedByDescending { it.timestamp } ?: emptyList()
            }
    }

    // 2️⃣ Resumen con formato de moneda
    val totalAmount = pendingList.sumOf { it.amount }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }
    val formattedTotal = currencyFormatter.format(totalAmount)

    // 3️⃣ Scaffold + FAB + Snackbar (sin cambios en la lógica)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = LightGreyBackground, // <<< CAMBIO: Fondo general más suave
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Lógica de refresco */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding) // <<< IMPORTANTE: Aplicar el padding del Scaffold
        ) {

            SummaryCard(
                totalAmount = formattedTotal,
                transactionCount = pendingList.size
            )

            // --- Lista de Transacciones Rediseñada ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingList, key = { it.id }) { transaction ->
                    // Usamos la animación que ya tenías
                    AnimatedVisibility(
                        visible = true,
                        enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                        exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
                    ) {
                        TransactionItemCard(
                            transaction = transaction,
                            currencyFormatter = currencyFormatter,
                            onClick = {
                                navController.navigate("deposit-validation/${transaction.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SummaryCard(totalAmount: String, transactionCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp), // <<< CAMBIO: Bordes más redondeados
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier

                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // <<< CAMBIO: Icono para dar contexto visual
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Billetera",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Total pendiente",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            "$transactionCount transacciones",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
                // <<< CAMBIO: Monto más grande y prominente
                Text(
                    text = totalAmount,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}


@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // <<< CAMBIO: Icono representativo a la izquierda
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pendiente",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            // <<< CAMBIO: Columna para concepto y estado
            Column(Modifier.weight(1f)) {
                Text(
                    // Mostramos el ID de forma más limpia, pero idealmente aquí iría un concepto
                    text = "ID: " + transaction.userId.take(15) + "...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                // <<< CAMBIO: Etiqueta de estado "Pendiente"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PendingStatusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Pendiente",
                        color = PendingStatusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // <<< CAMBIO: Monto a la derecha, con color y formato
            Text(
                text = currencyFormatter.format(transaction.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AmountColor
            )
        }
    }
}