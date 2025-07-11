package com.esan.payflowapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.esan.payflowapp.R
import com.esan.payflowapp.core.utils.toTwoDecimal
import com.esan.payflowapp.ui.viewmodel.DepositViewModel
import com.esan.payflowapp.ui.viewmodel.DepositViewModelFactory
import com.esan.payflowapp.ui.viewmodel.LoginViewModel

@Composable
fun DepositScreen(
    navController: NavController,
    viewModel: DepositViewModel = viewModel(factory = DepositViewModelFactory())
) {
    val context = LocalContext.current
    val balance by viewModel.balance.observeAsState(0.0)
    val state by viewModel.state.observeAsState()

    var destinyAccount by remember { mutableStateOf("") }
    var depositAmount by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            selectedPhoto = it
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getData(context)
    }

    LaunchedEffect(state) {
        if (state is DepositViewModel.DepositState.Success) {
            Toast.makeText(context, "Deposito creado", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } else if (state is DepositViewModel.DepositState.Fail) {
            Toast.makeText(
                context,
                (state as DepositViewModel.DepositState.Fail).message,
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
                        Text("Ingrese el número de cuenta")
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
                        Text("Ingrese el monto a depositar (S/)")
                    }
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(R.drawable.ic_clip),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("Adjuntar comprobante")
                    }
                }
                Spacer(Modifier.height(15.dp))
                selectedPhoto?.let { photoUri ->
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = null,
                        modifier = Modifier.size(240.dp)
                    )
                }
                Spacer(Modifier.height(15.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.createDeposit(
                            destinyAccount = destinyAccount,
                            amount = depositAmount.toDoubleOrNull() ?: 0.0
                        )
                    }
                ) {
                    Text("Realizar depósito")
                }
            }
        }
        if (state == DepositViewModel.DepositState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .alpha(0.5f)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(25.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun DepositScreen_Preview(modifier: Modifier = Modifier) {
//    DepositScreen()
//}