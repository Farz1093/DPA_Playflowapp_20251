package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esan.payflowapp.PayFlowApplication
import com.esan.payflowapp.R
import com.esan.payflowapp.data.repository.TransactionRepository
import com.esan.payflowapp.data.repository.UserRepository
import com.esan.payflowapp.ui.viewmodel.MainViewModel
import com.esan.payflowapp.ui.viewmodel.MainViewModelFactory
import com.esan.payflowapp.ui.views.TransactionRowView
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(
    // Creamos repositorios usando tu singleton de Room y el repo de usuario
    txRepo: TransactionRepository = remember {
        TransactionRepository(PayFlowApplication.database.transactionDao())
    },
    userRepo: UserRepository = remember { UserRepository() },
    // Obtenemos aquí el ViewModel con la factory
    vm: MainViewModel = viewModel(
        factory = MainViewModelFactory(txRepo, userRepo)
    ),
    modifier: Modifier = Modifier
) {

    // Recogemos el saldo y la lista de txs desde el StateFlow
    val balance by vm.balance.collectAsState(initial = 0L)
    val txs     by vm.recentTxs.collectAsState(initial = emptyList())

    Box(modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(25.dp))
            Image(
                modifier = Modifier.size(100.dp),
                painter = painterResource(R.drawable.ic_launcher_round),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(25.dp))

            // Saldo dinámico
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(end = 10.dp),
                    painter = painterResource(R.drawable.ic_money),
                    contentDescription = null
                )
                Text("Saldo disponible:")
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 34.dp),
                // Convertimos de centavos a soles (o adapta a tu unidad)
                text = "S/ ${"%,.2f".format(balance / 100f)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(25.dp))

            // Lista de transacciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(end = 10.dp),
                    painter = painterResource(R.drawable.ic_transaction),
                    contentDescription = null
                )
                Text("Últimos movimientos")
            }
            Spacer(Modifier.height(10.dp))

            LazyColumn {
                items(txs) { tx ->
                    TransactionRowView(tx)  // adapta este Composable para aceptar tx: TransactionEntity
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreen_Preview(modifier: Modifier = Modifier) {
    HomeScreen()
}