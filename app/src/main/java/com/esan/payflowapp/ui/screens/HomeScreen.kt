package com.esan.payflowapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esan.payflowapp.R
import com.esan.payflowapp.core.utils.toTwoDecimal
import com.esan.payflowapp.ui.model.GeneralState
import com.esan.payflowapp.ui.viewmodel.HomeViewModel
import com.esan.payflowapp.ui.viewmodel.HomeViewModelFactory
import com.esan.payflowapp.ui.views.TransactionRowView

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
) {
    val context = LocalContext.current
    val state by viewModel.state.observeAsState()
    val balance by viewModel.balance.observeAsState(0.0)
    val trxList by viewModel.trxList.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.getUserData(context)
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
            Image(
                modifier = Modifier
                    .size(100.dp),
                painter = painterResource(R.drawable.ic_launcher_round),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(25.dp))
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
                text = "S/ ${balance.toTwoDecimal()}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(25.dp))
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
                items(trxList) { trx ->
                    TransactionRowView(trx)
                }
            }
        }
        FloatingActionButton(
            onClick = { viewModel.getUserData(context) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Refresh, "Refrescar")
        }
        if (state == GeneralState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.25f))
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(25.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreen_Preview(modifier: Modifier = Modifier) {
    HomeScreen()
}