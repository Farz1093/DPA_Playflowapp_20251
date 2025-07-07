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
import com.esan.payflowapp.data.local.entities.TransactionEntity

@Composable
fun TransactionRowView(tx: TransactionEntity) {

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(tx.type, modifier = Modifier.weight(1f))
        Text("S/ ${tx.amount}", modifier = Modifier.weight(1f))
        Text(tx.status, modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransactionRowView_Preview(modifier: Modifier = Modifier) {
    val fakeTx = TransactionEntity(
        id        = "tx1",
        userId    = "user1",
        type      = "DEPOSIT",
        amount    = 150000L,
        status    = "PENDING",
        createdAt = System.currentTimeMillis(),
        currency     = "PEN",
        receiptUrl   = null,
        description  = "Depósito de prueba",
        updatedAt    = null,
        validatedBy  = null,
        validatedAt  = null
    )
    TransactionRowView(tx = fakeTx)
}