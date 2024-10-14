package com.example.tryggakampus.presentation.profilePage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun ProfilePage(title: String) {
    PageContainer {
        Text(title)
    }
}
