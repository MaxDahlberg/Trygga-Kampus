package com.example.tryggakampus

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

// A local to control the visibility of the top and bottom bars.
// Defaults to a state that shows them.
val LocalShowBars = compositionLocalOf<MutableState<Boolean>> {
    error("No ShowBarsState provided")
}
