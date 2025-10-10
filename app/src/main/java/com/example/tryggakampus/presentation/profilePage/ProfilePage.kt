package com.example.tryggakampus.presentation.profilePage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.presentation.component.*

@Composable
fun ProfilePage() {
    val vm: ProfileViewModel = viewModel<ProfileViewModel>()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ProfileHeader()

        Spacer(modifier = Modifier.height(20.dp))

        // Account information
        FormContainer {
            Text(
                text = "Account Information",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Username: ${vm.username}")
            Text(text = "Email: ${vm.email}")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ProfileHeader() {
    Text(
        text = "Profile",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}