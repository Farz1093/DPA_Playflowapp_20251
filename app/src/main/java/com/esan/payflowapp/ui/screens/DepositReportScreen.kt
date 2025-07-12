package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.esan.payflowapp.ui.navigation.DrawerScaffold
import com.esan.payflowapp.ui.viewmodel.DepositReportViewModel
import com.esan.payflowapp.ui.viewmodel.ReportUiState
import java.text.NumberFormat
import java.util.*

// El @Composable principal ahora debe incluir el NavController para pasarlo al DrawerScaffold
@Composable
fun DepositReportScreen(
    vm: DepositReportViewModel = viewModel()
) {

        val uiState by vm.uiState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center // Centra el CircularProgressIndicator
        ) {
            when (val state = uiState) {
                is ReportUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ReportUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
                is ReportUiState.Success -> {
                    val report = state.data
                    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }

                    LazyColumn(
                        // El padding del LazyColumn ahora es solo para el espaciado interno
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize() // Alinear al TopStart del Box
                    ) {
                        item {
                            Text(
                                "Resumen de Estados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            ReportCard {
                                // ... (Esta tarjeta no cambia)
                                ReportKpi(Icons.Default.HourglassTop, "Depósitos Pendientes", report.pendingCount.toString(), Color.Blue)
                                Divider()
                                ReportKpi(Icons.Default.CheckCircle, "Depósitos Completados", report.completedCount.toString(), Color(0xFF34A853))
                                Divider()
                                ReportKpi(Icons.Default.Cancel, "Depósitos Rechazados", report.failedCount.toString(), Color(0xFFEA4335))
                            }
                        }

                        item {
                            Text(
                                "Métricas Clave",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        item {
                            ReportCard {
                                ReportKpi(
                                    icon = Icons.Default.TrendingUp,
                                    label = "Monto Total Completado",
                                    value = currencyFormatter.format(report.totalAmountCompleted),
                                    color = Color(0xFF34A853) // Verde
                                )
                                Divider()

                                // <<< NUEVO: KPI para Monto Pendiente >>>
                                ReportKpi(
                                    icon = Icons.Default.QueryBuilder,
                                    label = "Monto Total Pendiente",
                                    value = currencyFormatter.format(report.totalAmountPending),
                                    color = Color.Blue
                                )
                                Divider()

                                // <<< NUEVO: KPI para Monto Rechazado >>>
                                ReportKpi(
                                    icon = Icons.Default.MoneyOff,
                                    label = "Monto Total Rechazado",
                                    value = currencyFormatter.format(report.totalAmountFailed),
                                    color = Color(0xFFEA4335) // Rojo
                                )
                                Divider()

                                ReportKpi(
                                    icon = Icons.Default.Timer,
                                    label = "Tiempo Promedio de Validación",
                                    value = "≈ ${"%.2f".format(report.averageValidationTimeHours)} horas",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

}


@Composable
private fun ReportCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large // Bordes más redondeados
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun ReportKpi(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp) // Tamaño ligeramente más pequeño para los iconos internos
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge, // Un poco más grande para el valor
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}