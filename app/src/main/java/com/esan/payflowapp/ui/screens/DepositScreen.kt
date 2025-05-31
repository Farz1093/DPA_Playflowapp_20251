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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esan.payflowapp.R

@Composable
fun DepositScreen(modifier: Modifier = Modifier) {
    var destinyAccount by remember { mutableStateOf("") }
    var depositAmount by remember { mutableStateOf("") }
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
                    text = "S/ 100,000.00",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
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
                        keyboardType = KeyboardType.Number
                    ),
                    placeholder = {
                        Text("Ingrese el monto a depositar (S/)")
                    }
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        //SOMETHING
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
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        //SOMETHING
                    }
                ) {
                    Text("Realizar depósito")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DepositScreen_Preview(modifier: Modifier = Modifier) {
    DepositScreen()
}