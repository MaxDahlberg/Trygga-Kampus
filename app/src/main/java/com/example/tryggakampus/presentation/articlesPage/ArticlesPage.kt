package com.example.tryggakampus.presentation.articlesPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ArticlesPage(title: String) {
    Column (modifier = Modifier.fillMaxWidth()) {
        Text(title)
    }
}
