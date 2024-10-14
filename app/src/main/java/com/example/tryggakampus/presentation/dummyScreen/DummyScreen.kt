package com.example.tryggakampus.presentation.dummyScreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun DummyScreen(title: String) {
    PageContainer {
        Text(title)
    }
}
