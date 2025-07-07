package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esan.payflowapp.R
import com.esan.payflowapp.data.local.entities.TransactionEntity
import com.esan.payflowapp.ui.views.TransactionRowView

@Composable
fun TransactionsHistoryScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        items(count = 10) { index ->
            // Crea un TransactionEntity mínimo para el placeholder
            val fakeTx = TransactionEntity(
                id        = "tx$index",
                userId    = "user1",
                type      = if (index % 2 == 0) "DEPOSIT" else "WITHDRAW",
                amount    = 1000L * (index + 1),
                status    = if (index % 3 == 0) "PENDING" else "COMPLETED",
                createdAt = System.currentTimeMillis(),
                currency     = "PEN",
                receiptUrl   = null,
                description  = null,
                updatedAt    = null,
                validatedBy  = null,
                validatedAt  = null,
            )
            TransactionRowView(tx = fakeTx)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransactionsHistoryScreen_Preview(modifier: Modifier = Modifier) {
    TransactionsHistoryScreen()
}