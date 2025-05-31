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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun DepositValidationScreen(modifier: Modifier = Modifier) {
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
                Text("Usuario:", fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text("DPA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monto:", fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text("S/ 50.00", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fecha:", fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text("24/04/2025", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(15.dp))
            TextButton(onClick = {
                //DO SOMETHING
            }) {
                Text("Ver comprobante")
            }
            Spacer(Modifier.height(25.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    //SOMETHING
                }
            ) {
                Text("Aprobar")
            }
            Spacer(Modifier.height(5.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    //SOMETHING
                }
            ) {
                Text("Rechazar")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DepositValidationScreen_Preview(modifier: Modifier = Modifier) {
    DepositValidationScreen()
}