package com.esan.payflowapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.esan.payflowapp.ui.navigation.DrawerScaffold
import com.esan.payflowapp.ui.viewmodel.*
import java.text.NumberFormat
import java.util.*
import kotlin.random.Random

private val cardColor = Color(0xFFFFFFFF)
private val screenBgColor = Color(0xFFF0F2F5)
private val textColorPrimary = Color(0xFF1C202A)
private val textColorSecondary = Color(0xFF6A7383)
private val depositColor = Color(0xFF007BFF)
private val spentColor = Color(0xFFFFA000)

@Composable
fun AdvancedDepositReportScreen(
    vm: AdvancedDepositReportViewModel = viewModel()
) {
  // El Scaffold nos da el padding que necesitamos.

        // El resto de tu UI va aquí dentro, usando los paddingValues que nos da el Scaffold.
        val uiState by vm.uiState.collectAsState()

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = screenBgColor
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is AdvancedReportUiState.Loading -> CircularProgressIndicator()
                    is AdvancedReportUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                    is AdvancedReportUiState.Success -> {
                        val report = state.data
                        val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "PE")) }

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
                        ) {
                            LazyColumn(
                                // Ya no necesitamos pasar el padding aquí porque el contenedor padre (Surface) ya lo tiene.
                                // Pero añadimos un padding horizontal propio para el diseño.
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                item { Spacer(Modifier.height(4.dp)) }

                                item {
                                    ReportSectionTitle(
                                        title = "Flujo de Fondos",
                                        subtitle = "Últimos 90 días"
                                    )
                                    ComparisonBarChart(
                                        deposited = report.totalDepositedInPeriod.toFloat(),
                                        spent = report.totalSpentByDepositorsInPeriod.toFloat(),
                                        formatter = currencyFormatter
                                    )
                                }

                                if (report.topDepositors.isNotEmpty()) {
                                    item { ReportSectionTitle("Top Depositantes (Ballenas)") }
                                    items(report.topDepositors, key = { it.uid }) { user ->
                                        UserHighlightCard(user = user, formatter = currencyFormatter, type = UserCardType.DEPOSIT)
                                    }
                                }

                                if (report.mostActiveUsers.isNotEmpty()) {
                                    item { ReportSectionTitle("Usuarios Más Activos") }
                                    items(report.mostActiveUsers, key = { it.uid + "-active" }) { user ->
                                        UserHighlightCard(user = user, formatter = currencyFormatter, type = UserCardType.ACTIVITY)
                                    }
                                }

                                if (report.dormantWhales.isNotEmpty()) {
                                    item { ReportSectionTitle("Ballenas Dormidas (En Riesgo)", icon = Icons.Default.Warning) }
                                    items(report.dormantWhales, key = { it.uid + "-dormant" }) { user ->
                                        UserHighlightCard(user = user, formatter = currencyFormatter, type = UserCardType.DORMANT)
                                    }
                                }
                                item { Spacer(Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }

    }
}

@Composable
private fun ReportSectionTitle(title: String, subtitle: String? = null, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = title, tint = spentColor, modifier = Modifier.padding(end = 8.dp))
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColorPrimary)
        if (subtitle != null) {
            Spacer(Modifier.width(8.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = textColorSecondary)
        }
    }
}

@Composable
private fun ComparisonBarChart(deposited: Float, spent: Float, formatter: NumberFormat) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            val maxVal = maxOf(deposited, spent, 1f)
            ChartRow(
                label = "Total Depositado",
                value = deposited,
                color = depositColor,
                maxValue = maxVal,
                formattedValue = formatter.format(deposited)
            )
            Spacer(Modifier.height(16.dp))
            ChartRow(
                label = "Total Gastado (por depositantes)",
                value = spent,
                color = spentColor,
                maxValue = maxVal,
                formattedValue = formatter.format(spent)
            )
        }
    }
}

@Composable
private fun ChartRow(label: String, value: Float, color: Color, maxValue: Float, formattedValue: String) {
    val animatedFraction by animateFloatAsState(targetValue = if(maxValue > 0) value / maxValue else 0f, label = "barAnimation")

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = textColorSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(formattedValue, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = animatedFraction)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(8.dp))
            )
        }
    }
}

private enum class UserCardType { DEPOSIT, ACTIVITY, DORMANT }

@Composable
private fun UserHighlightCard(user: UserReportInfo, formatter: NumberFormat, type: UserCardType) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info
            Column(Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, color = textColorPrimary, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                val highlightText = when(type) {
                    UserCardType.DEPOSIT -> "Depositado: ${formatter.format(user.totalDeposited)}"
                    UserCardType.ACTIVITY -> "Gastado: ${formatter.format(user.totalSpent)} (${user.transactionCount} txns)"
                    UserCardType.DORMANT -> "Depositó ${formatter.format(user.totalDeposited)} y está inactivo"
                }
                Text(
                    text = highlightText,
                    color = if(type == UserCardType.DORMANT) spentColor else textColorSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (type == UserCardType.DORMANT) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Sparkline (mini gráfico)
            Sparkline(Modifier.size(width = 50.dp, height = 30.dp), color = if (type == UserCardType.DORMANT) Color.Gray else depositColor)
        }
    }
}

@Composable
private fun Sparkline(modifier: Modifier = Modifier, color: Color) {
    val randomPoints = remember { List(10) { 1f - Random.nextFloat() } }
    Canvas(modifier = modifier) {
        val path = Path()
        val pathWidth = size.width
        val pathHeight = size.height

        path.moveTo(0f, randomPoints[0] * pathHeight)
        randomPoints.forEachIndexed { index, point ->
            if (index > 0) {
                val x = (index.toFloat() / (randomPoints.size - 1)) * pathWidth
                val y = point * pathHeight
                path.lineTo(x, y)
            }
        }
        drawPath(path, color, style = Stroke(width = 4f))
    }
}