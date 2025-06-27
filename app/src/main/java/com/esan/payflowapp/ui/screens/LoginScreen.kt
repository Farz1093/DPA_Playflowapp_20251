package com.esan.payflowapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.esan.payflowapp.R
import com.esan.payflowapp.ui.viewmodel.LoginViewModel
import com.esan.payflowapp.ui.viewmodel.LoginViewModelFactory

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory())
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.observeAsState()

    val context = LocalContext.current

    BackHandler {
        //NONE
    }

    LaunchedEffect(state) {
        if (state is LoginViewModel.LoginState.Success) {
            navController.navigate("home")
        } else if (state is LoginViewModel.LoginState.Fail) {
            Toast.makeText(context, (state as LoginViewModel.LoginState.Fail).message, Toast.LENGTH_SHORT).show()
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pay Flow",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(15.dp))
            Image(
                modifier = Modifier
                    .size(100.dp),
                painter = painterResource(R.drawable.ic_launcher_round),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = {
                    username = it
                },
                maxLines = 1,
                placeholder = {
                    Text("Usuario")
                }
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = {
                    password = it
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                maxLines = 1,
                placeholder = {
                    Text("Contraseña")
                }
            )
            Spacer(Modifier.height(15.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.doLogin(context, username, password)
                }) {
                Text("Ingresar")
            }
        }

        if (state == LoginViewModel.LoginState.Loading) {
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