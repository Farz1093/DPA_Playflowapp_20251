package com.esan.payflowapp.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.esan.payflowapp.R
import com.esan.payflowapp.core.utils.toTwoDecimal
import com.esan.payflowapp.ui.model.GeneralState
import com.esan.payflowapp.ui.viewmodel.TransferViewModel
import com.esan.payflowapp.ui.viewmodel.TransferViewModelFactory

@Composable
fun TransferScreen(
    navController: NavController,
    viewModel: TransferViewModel = viewModel(factory = TransferViewModelFactory())
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val balance by viewModel.balance.observeAsState(0.0)
    val state by viewModel.state.observeAsState()

    var destinyAccount by remember { mutableStateOf("") }
    var depositAmount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getData(context)
    }

    LaunchedEffect(state) {
        if (state is GeneralState.Success) {
            Toast.makeText(context, "Transferencia realizada", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } else if (state is GeneralState.Fail) {
            Toast.makeText(
                context,
                (state as GeneralState.Fail).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(25.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        painter = painterResource(R.drawable.baseline_account_circle_24),
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
                Spacer(Modifier.height(15.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = destinyAccount,
                    onValueChange = {
                        destinyAccount = it
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    placeholder = {
                        Text("Ingrese el número de cuenta destino")
                    }
                )
                Spacer(Modifier.height(15.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = depositAmount,
                    onValueChange = {
                        depositAmount = it
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    placeholder = {
                        Text("Ingrese el monto a transferir (S/)")
                    }
                )
                Spacer(Modifier.height(25.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        keyboard?.hide()
                        viewModel.transferMoney(
                            destinyAccount = destinyAccount,
                            amount = depositAmount.toDoubleOrNull() ?: 0.0
                        )
                    }
                ) {
                    Text("Realizar transferencia")
                }
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
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun TransferScreen_Preview(modifier: Modifier = Modifier) {
//    TransferScreen()
//}