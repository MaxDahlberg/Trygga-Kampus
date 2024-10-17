package com.example.tryggakampus.presentation.formPage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.tryggakampus.presentation.component.PageContainer


@Composable
fun FormPage(title: String) {
    PageContainer {
        Text(title)
    }
}