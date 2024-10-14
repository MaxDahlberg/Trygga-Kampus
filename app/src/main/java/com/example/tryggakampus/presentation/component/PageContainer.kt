package com.example.tryggakampus.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PageContainer(
    modifier: Modifier = Modifier,
    children: @Composable()() -> Unit
) {
    Column (modifier = modifier
        .fillMaxWidth()
        .padding(top = 0.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        .verticalScroll(rememberScrollState())
    ) {
        children()
    }
}
