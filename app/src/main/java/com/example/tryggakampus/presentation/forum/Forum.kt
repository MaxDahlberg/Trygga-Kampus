package com.example.tryggakampus.presentation.forum

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun Forum(title: String) {
    PageContainer {
        Text(title)
    }
}