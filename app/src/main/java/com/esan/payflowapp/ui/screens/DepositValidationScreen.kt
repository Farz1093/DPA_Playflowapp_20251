package com.esan.payflowapp.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandIn
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
import com.esan.payflowapp.core.firebase.models.TransactionWithUser
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.data.local.entities.UserEntity
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility // (No la de ColumnScope)
import com.esan.payflowapp.core.firebase.models.UserProfile


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
    navController: NavController
) {

    var pendingListWithUsers by remember { mutableStateOf<List<TransactionWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // <<< CAMBIO CLAVE: La lógica de carga ahora busca transacciones Y usuarios >>>
    LaunchedEffect(Unit) {
        // 1. Un único listener para las transacciones. Se disparará cada vez que algo cambie.
        firestore.collection("transactions")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { transactionSnaps, error ->
                if (error != null) {
                    isLoading = false

                    return@addSnapshotListener
                }

                // Convertimos las transacciones recibidas.
                val transactions = transactionSnaps?.toObjects(TransactionEntity::class.java) ?: emptyList()

                if (transactions.isEmpty()) {
                    pendingListWithUsers = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }

                // 2. Por cada actualización, lanzamos una corrutina para buscar los nombres.
                coroutineScope.launch {
                    try {
                        val userIds = transactions.map { it.userId }.distinct().filter { it.isNotBlank() }

                        if (userIds.isEmpty()) {
                            // Si no hay IDs, mostramos las transacciones sin nombre.
                            pendingListWithUsers = transactions.map { TransactionWithUser(it, "ID de usuario vacío") }
                                .sortedByDescending { it.transaction.createdAt }
                            isLoading = false
                            return@launch
                        }

                        // 3. Hacemos una lectura ÚNICA de los usuarios necesarios.
                        val usersSnapshot = firestore.collection("users")
                            .whereIn(FieldPath.documentId(), userIds)
                            .get()
                            .await()

                        val usersMap = usersSnapshot.documents.associate { document ->
                            document.id to document.toObject(UserProfile::class.java)
                        }

                        // 4. Combinamos los datos y actualizamos la UI.
                        val combinedList = transactions.map { transaction ->
                            TransactionWithUser(
                                transaction = transaction,
                                userName = usersMap[transaction.userId]?.name ?: "Usuario no encontrado"
                            )
                        }

                        // La ordenación se hace aquí, al final, antes de mostrar.
                        pendingListWithUsers = combinedList.sortedByDescending { it.transaction.createdAt }
                        isLoading = false

                    } catch (e: Exception) {

                        // Como fallback, mostramos la lista sin nombres.
                        pendingListWithUsers = transactions.map { TransactionWithUser(it, "Error al cargar nombre") }
                            .sortedByDescending { it.transaction.createdAt }
                        isLoading = false
                    }
                }
            }
    }

    // --- Resumen y Scaffold ---
    val totalAmount = pendingListWithUsers.sumOf { it.transaction.amount }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }
    val formattedTotal = currencyFormatter.format(totalAmount)

    Scaffold(
        containerColor = LightGreyBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Lógica de refresco manual si la necesitas */ },
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

            // <<< CAMBIO CLAVE: Usamos un Box para mostrar el indicador de carga >>>
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // <<< CAMBIO CLAVE: Iteramos sobre la lista combinada >>>
                        items(pendingListWithUsers, key = { it.transaction.id }) { item ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                                exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
                            ) {
                                TransactionItemCard(
                                    item = item, // Pasamos el objeto combinado
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


// <<< CAMBIO CLAVE: El Card ahora recibe el objeto TransactionWithUser >>>
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
                // <<< ¡AQUÍ ESTÁ LA MAGIA! Mostramos el nombre del usuario >>>
                Text(
                    text = item.userName ?: "Usuario no encontrado", // Usamos el nombre del usuario
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