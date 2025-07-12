package com.esan.payflowapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esan.payflowapp.R
import com.esan.payflowapp.core.firebase.model.Transaction
import com.esan.payflowapp.core.utils.toTwoDecimal
import com.esan.payflowapp.ui.viewmodel.HistoryTrxViewModel
import com.esan.payflowapp.ui.viewmodel.HistoryTrxViewModelFactory
import com.esan.payflowapp.ui.viewmodel.HomeViewModelFactory
import com.esan.payflowapp.ui.viewmodel.RangeFilter
import com.esan.payflowapp.ui.views.TransactionRowView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsHistoryScreen(
    viewModel: HistoryTrxViewModel = viewModel(factory = HistoryTrxViewModelFactory())
) {
    var trxList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var filter by remember { mutableStateOf(RangeFilter.TODAY) }
    var customFrom by remember { mutableStateOf<Long?>(null) }
    var customTo by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(filter, customTo, customFrom) {
        viewModel.getHistory(filter, customFrom, customTo)
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(25.dp))
            Row {
                Button(
                    onClick = { filter = RangeFilter.TODAY },
                    colors = if (filter == RangeFilter.TODAY) ButtonDefaults.buttonColors()
                    else ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Hoy") }

                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { filter = RangeFilter.WEEK },
                    colors = if (filter == RangeFilter.WEEK) ButtonDefaults.buttonColors()
                    else ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Última semana") }

                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { filter = RangeFilter.RANGE },
                    colors = if (filter == RangeFilter.RANGE) ButtonDefaults.buttonColors()
                    else ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Rango")
                }
            }
            if (filter == RangeFilter.RANGE) {
                Spacer(Modifier.height(8.dp))
                Row {
                    DatePickerButton("Desde", customFrom) { customFrom = it }
                    Spacer(Modifier.width(8.dp))
                    DatePickerButton("Hasta", customTo) { customTo = it }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (trxList.isEmpty()) {
                Text("No hay transacciones en el periodo seleccionado.")
            } else {
                LazyColumn {
                    items(trxList) { trx ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    if (trx.type == "deposit") Color(0xFFFFF9C4)
                                    else if (trx.type == "transfer_sent") Color(0xFFE3F2FD)
                                    else Color(0xFFC8E6C9)
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(trx.getTrxType(), fontWeight = FontWeight.Bold)
                                Text("Monto: ${trx.amount.toTwoDecimal()}")
                                if (trx.isDeposit())
                                    Text("Estado: ${trx.getDepositStatus()}")
                                Text(
                                    text = "Fecha: ${
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                            .format(Date(trx.date))
                                    }",
                                    fontSize = 12.sp,
                                    color = Color.Gray
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
fun DatePickerButton(label: String, value: Long?, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()
    if (value != null) cal.timeInMillis = value
    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(y, m, d, 0, 0, 0)
            selectedCal.set(Calendar.MILLISECOND, 0)
            onDateSelected(selectedCal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }
    OutlinedButton(onClick = { datePickerDialog.show() }) {
        Text(
            "$label: " + if (value != null) SimpleDateFormat(
                "dd/MM/yyyy", Locale("es", "PE")
            ).format(Date(value)) else "--/--/----"
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransactionsHistoryScreen_Preview(modifier: Modifier = Modifier) {
    TransactionsHistoryScreen()
}