package com.example.tryggakampus.presentation.profilePage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfilePage(title: String) {
    Column (modifier = Modifier.fillMaxWidth()) {
        Text(title)
    }
}
