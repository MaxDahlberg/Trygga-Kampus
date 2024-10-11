package com.example.tryggakampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.tryggakampus.presentation.MainScreen
import com.example.tryggakampus.ui.theme.TryggaKampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TryggaKampusTheme {
                Navigation { pageToView -> MainScreen(pageToView) }
            }
        }
    }
}
