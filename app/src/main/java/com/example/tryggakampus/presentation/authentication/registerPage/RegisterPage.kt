package com.example.tryggakampus.presentation.authentication.registerPage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.R
import com.example.tryggakampus.Routes
import com.example.tryggakampus.presentation.component.BlockButton
import com.example.tryggakampus.presentation.component.FormContainer
import com.example.tryggakampus.presentation.component.ErrorBox
import com.example.tryggakampus.presentation.component.OutlinedInput

@Composable
fun RegisterPage() {
    val vm: RegisterViewModel = viewModel<RegisterViewModel>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RegisterFormHeader()

        FormContainer {
            OutlinedInput(
                label = "Email",
                value = vm.email,
                onValueChange = { vm.onEmailChange(it) },
                isError = !vm.emailIsValid
            )

            OutlinedInput(
                label = "Password",
                value = vm.password,
                onValueChange = { vm.onPasswordChange(it) },
                isError = !vm.passwordIsValid,
                showPasswordRules = true,
                onVisibilityChange = { vm.togglePasswordVisibility() },
                isPassword = true,
                isPasswordVisible = vm.isPasswordVisible

            )

            BlockButton (
                onClick = { if (!vm.signingUp) vm.onRequestSignUp() },
                enabled = (vm.emailIsValid && vm.passwordIsValid)
            ) {
                if (vm.signingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                } else {
                    Text("Sign Up")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        vm.error?.let {
            ErrorBox(it.message, onClick = { vm.clearError() })
        }

        Spacer(modifier = Modifier.size(30.dp))
        RegisterFormFooter()
    }
}

@Composable
fun RegisterFormHeader() {
    Image(
        painter = painterResource(id = R.drawable.trygga_kampus_new_logo),
        contentDescription = "Trygga Kampus Logo",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
    Spacer(modifier = Modifier.size(10.dp))
}

@Composable
fun RegisterFormFooter() {
    val navController = LocalNavController.current

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Already registered?")
        Button(onClick = { navController.navigate(Routes.Authentication.LoginPage) }) {
            Text(text = "Sign In", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
