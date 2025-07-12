package com.esan.payflowapp.ui.screens

import android.util.Log
import androidx.compose.animation.*
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
import com.esan.payflowapp.core.firebase.model.DepositTransaction
import com.esan.payflowapp.core.firebase.model.TransactionWithUser
import com.esan.payflowapp.core.firebase.model.UserDataDeposit
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

// --- Definiciones de colores (sin cambios) ---
val LightGreyBackground = Color(0xFFF5F7FA)
val GradientStart = Color(0xFF6A82FB)
val GradientEnd = Color(0xFF7A6AF8)
val AmountColor = Color(0xFF2E7D32)
val PendingStatusBg = Color(0xFFFFF0C1)
val PendingStatusText = Color(0xFF8C5B00)
val IconBgColor = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositValidationScreen(
    navController: NavController
) {
    var pendingListWithUsers by remember { mutableStateOf<List<TransactionWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        firestore.collection("deposit_data")
            .whereEqualTo("status", "PENDING")
            //.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { transactionSnaps, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (transactionSnaps == null || transactionSnaps.isEmpty) {
                    pendingListWithUsers = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }

                val transactions = transactionSnaps.documents.map { doc ->
                    doc.toObject(DepositTransaction::class.java)!!.copy(id = doc.id)
                }

                coroutineScope.launch {
                    try {
                        val userIds = transactions.map { it.uid }.distinct().filter { it.isNotBlank() }

                        if (userIds.isEmpty()) {
                            pendingListWithUsers = transactions.map { TransactionWithUser(it, "ID de usuario vacío") }
                            isLoading = false
                            return@launch
                        }

                        val usersSnapshot = firestore.collection("user_data")
                            .whereIn(FieldPath.documentId(), userIds)
                            .get()
                            .await()


                        val usersMap = usersSnapshot.documents.associate { doc ->
                            doc.id to doc.toObject(UserDataDeposit::class.java)
                        }

                        // <<< CAMBIO 3: La lógica para obtener el nombre es la misma, ¡porque el campo se llama igual! >>>
                        val combinedList = transactions.map { transaction ->
                            TransactionWithUser(
                                transaction = transaction,
                                userName = usersMap[transaction.uid]?.name ?: "Usuario no encontrado"
                            )
                        }
                        pendingListWithUsers = combinedList.sortedByDescending { it.transaction.createdAt }
                        pendingListWithUsers = combinedList
                        isLoading = false

                    } catch (e: Exception) {
                        Log.e("VALIDATION_SCREEN", "¡FALLO AL PROCESAR DATOS!", e)
                        pendingListWithUsers = transactions.map { TransactionWithUser(it, "Error al cargar nombre") }
                        isLoading = false
                    }
                }
            }
    }

    val totalAmount = pendingListWithUsers.sumOf { it.transaction.amount }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }
    val formattedTotal = currencyFormatter.format(totalAmount)

    Scaffold(
        // ... El resto del Scaffold y la UI no necesitan cambios ...
        containerColor = LightGreyBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Lógica de refresco */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) { Icon(Icons.Default.Refresh, "Refrescar") }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SummaryCard(
                totalAmount = formattedTotal,
                transactionCount = pendingListWithUsers.size
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (pendingListWithUsers.isEmpty()) {
                    Text("No hay transacciones pendientes.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingListWithUsers, key = { it.transaction.id }) { item ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                                exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
                            ) {
                                TransactionItemCard(
                                    item = item,
                                    currencyFormatter = currencyFormatter,
                                    onClick = {
                                        navController.navigate("deposit-validation/${item.transaction.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... SummaryCard y TransactionItemCard permanecen exactamente iguales ...
@Composable
fun SummaryCard(totalAmount: String, transactionCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.background(Brush.horizontalGradient(colors = listOf(GradientStart, GradientEnd)))) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, "Billetera", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Total pendiente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                        Text("$transactionCount transacciones", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
                Text(totalAmount, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    item: TransactionWithUser,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(IconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, "Pendiente", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(PendingStatusBg).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Pendiente", color = PendingStatusText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = currencyFormatter.format(item.transaction.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AmountColor
            )
        }
    }
}