package com.esan.payflowapp.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TransactionRowView(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row {
                Text("Monto:")
                Spacer(Modifier.width(5.dp))
                Text("S/ 500.00", fontWeight = FontWeight.Bold)
            }
            Row {
                Text("Tipo de transacción:")
                Spacer(Modifier.width(5.dp))
                Text("Retiro", fontWeight = FontWeight.Bold)
            }
            Row {
                Text("Fecha y hora:")
                Spacer(Modifier.width(5.dp))
                Text("20/05/2025 14:00:00", fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransactionRowView_Preview(modifier: Modifier = Modifier) {
    TransactionRowView()
}