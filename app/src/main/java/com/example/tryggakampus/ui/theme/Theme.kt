package com.example.tryggakampus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF282E35),
    onBackground = Color(0xFFD1D1D1),
    secondary = Color(0xFF89E6F0),
    onSecondary = Color(0xFF023A40),
    primary = Color(0xFF32383F),
    onPrimary = Color(0xFFC9D6E5),
    error = Color(0xFFFF4B4B),
    errorContainer = Color(0xFFFF4B4B),
    onErrorContainer = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFEEEEEE),
    onBackground = Color(0xFF282E35),
    primary = Color(0xFFFBFBFB),
    onPrimary = Color(0xFF3A3A3A),
    secondary = Color(0xFF2DC3D3),
    onSecondary = Color(0xFFFFFFFF),
    error = Color(0xFFFF4B4B),
    errorContainer = Color(0xFFFF4B4B),
    onErrorContainer = Color(0xFFFFFFFF)
)

@Composable
fun TryggaKampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}