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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esan.payflowapp.PayFlowApplication
import com.esan.payflowapp.R
import com.esan.payflowapp.data.repository.TransactionRepository
import com.esan.payflowapp.ui.viewmodel.DepositViewModel
import com.esan.payflowapp.ui.viewmodel.DepositViewModelFactory

@Composable
fun DepositScreen(
    navBack: ()->Unit,
    repo: TransactionRepository = remember { TransactionRepository(PayFlowApplication.database.transactionDao()) },
    vm: DepositViewModel = viewModel(factory = DepositViewModelFactory(repo))
) {
    val amount   by vm.amount.collectAsState()
    val loading  by vm.loading.collectAsState()
    val error    by vm.error.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Registrar Depósito", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = amount,
            onValueChange = vm::onAmountChange,
            label = { Text("Monto (en centavos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = vm::submitDeposit,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(24.dp))
            else Text("Enviar Depósito")
        }

        error?.let { Text(it, color = Color.Red) }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DepositScreen_Preview(modifier: Modifier = Modifier) {
    DepositScreen(
        navBack = {}
    )
}