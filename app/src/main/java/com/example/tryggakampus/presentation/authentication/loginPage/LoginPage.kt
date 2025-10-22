package com.example.tryggakampus.presentation.authentication.loginPage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.R
import com.example.tryggakampus.Routes
import com.example.tryggakampus.presentation.component.BlockButton
import com.example.tryggakampus.presentation.component.ErrorBox
import com.example.tryggakampus.presentation.component.FormContainer
import com.example.tryggakampus.presentation.component.OutlinedInput
import com.example.tryggakampus.presentation.component.SuccessBox
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

@Composable
fun LoginPage() {
    val vm: LoginViewModel = viewModel<LoginViewModel>()
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("987815613065-v2hc0082pjvsehu5q5oe0028ch4b877o.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            vm.onSignInWithGoogle(idToken)
        } catch (e: ApiException) {
            vm.showSignInError("Google sign-in failed. Please try again.")
        }
    }
    val callbackManager = remember { CallbackManager.Factory.create() }
    val facebookAuthLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(callbackManager, null)
    ) { /* The result is handled in the callback below */ }


    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                vm.onSignInWithFacebook(result.accessToken.token)
            }

            override fun onCancel() {
                vm.showSignInError("Facebook sign-in was cancelled.")
            }

            override fun onError(error: FacebookException) {
                vm.showSignInError("Facebook sign-in failed.")
            }

        })


        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (vm.passwordResetEmailSent) {
            SuccessBox(
                message = "A password reset link has been sent to your email. Please check your inbox.",
                onClick = { vm.dismissPasswordResetMessage() }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LoginFormHeader()

        FormContainer {
            OutlinedInput(
                label = "email@company.xyz",
                value = vm.email,
                onValueChange = { vm.onEmailChange(it) },
                isError = !vm.emailIsValid,
            )

            OutlinedInput(
                label = "Password",
                value = vm.password,
                onValueChange = { vm.onPasswordChange(it) },
                isError = !vm.passwordIsValid,
                isPassword = true,
                isPasswordVisible = vm.isPasswordVisible,
                showPasswordRules = true,
                onVisibilityChange = { vm.togglePasswordVisibility() },
            )

            ForgotPasswordButton(
                onClick = { vm.onForgotPassword() },
                enabled = !vm.signingIn
            )

            Spacer(modifier = Modifier.height(16.dp))

            BlockButton(
                onClick = { vm.onRequestLogin() },
                enabled = (vm.emailIsValid && vm.passwordIsValid && !vm.signingIn)
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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialLoginIcon(
                iconId = R.drawable.ic_google_logo,
                contentDescription = "Sign in with Google",
                enabled = !vm.signingIn,
                onClick = { googleAuthLauncher.launch(googleSignInClient.signInIntent) }
            )
            Spacer(modifier = Modifier.width(24.dp))

            SocialLoginIcon(
                iconId = R.drawable.ic_facebook_logo,
                contentDescription = "Sign in with Facebook",
                enabled = !vm.signingIn,
                onClick = {
                    facebookAuthLauncher.launch(listOf("email", "public_profile"))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        vm.error?.let {
            ErrorBox(it.message, onClick = { vm.clearError() })
        }

        Spacer(modifier = Modifier.weight(1f))

        LoginFormFooter()
    }
}


@Composable
fun SocialLoginIcon(
    iconId: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = contentDescription
        )
    }
}


@Composable
fun ForgotPasswordButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onClick, enabled = enabled) {
            Text(
                text = "Forgot Password?",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun LoginFormHeader() {
    Image(
        painter = painterResource(id = R.drawable.trygga_kampus_new_logo),
        contentDescription = "Trygga Kampus Logo",
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    )
    Spacer(modifier = Modifier.size(10.dp))
}

@Composable
fun LoginFormFooter() {
    val navController = LocalNavController.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Need an account?")
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { navController.navigate(Routes.Authentication.RegisterPage) }) {
            Text(text = "Sign Up")
        }
    }
}
