package com.example.tryggakampus.presentation.authentication.loginPage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.Routes
import com.example.tryggakampus.presentation.component.BlockButton
import com.example.tryggakampus.presentation.component.FormContainer
import com.example.tryggakampus.presentation.component.ErrorBox

import com.example.tryggakampus.presentation.component.OutlinedInput

@Composable
fun LoginPage() {
    val vm: LoginViewModel = viewModel<LoginViewModel>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginFormHeader()

        FormContainer {
            OutlinedInput(
                label = "email@company.xyz",
                value = vm.email,
                onValueChange = { vm.onEmailChange(it) },
                isError = !vm.emailIsValid
            )

            OutlinedInput(
                label = "Password",
                value = vm.password,
                onValueChange = { vm.onPasswordChange(it) },
                isError = !vm.passwordIsValid
            )

            BlockButton (
                onClick = { vm.onRequestLogin() },
                enabled = (vm.emailIsValid && vm.passwordIsValid)
            ) {
                if (vm.signingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                } else {
                    Text("Sign In")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        vm.error?.let {
            ErrorBox(it.message, onClick = { vm.clearError() })
        }

        Spacer(modifier = Modifier.size(30.dp))
        LoginFormFooter()
    }
}

@Composable
fun LoginFormHeader() {
    Text(
        text = "Trygga Campus - Sign In",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.size(30.dp))
}

@Composable
fun LoginFormFooter() {
    val navController = LocalNavController.current

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Need an account?")
        Button(onClick = { navController.navigate(Routes.Authentication.RegisterPage) }) {
            Text(text = "Sign Up", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginFeature() {
    LoginPage()
}
