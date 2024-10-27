package com.example.tryggakampus.presentation.advicePage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.R

import com.example.tryggakampus.domain.model.AdviceItem

// this is the screen that will show each card related to the category picked
@Composable
fun AdviceListScreen(title: String, adviceItems: List<AdviceItem>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.small,
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text("Back")
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(adviceItems) { adviceItem ->
                AdviceCard(adviceItem = adviceItem)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdviceListScreenPreview() {
    AdviceListScreen(
        title = "Just A Test Title Again",
        adviceItems = listOf(
            AdviceItem(
                title = "Advice 1",
                text = "Just writing some text in order to test how it will look in the card" +
                        "and making it a bit longer too so that I can test the hiding the text if its too" +
                        "long and then testing the expand button too so at this point this is just rambling" +
                        "text in order to fill the space size because I dont want more than 3 lines showing.",
                image = R.drawable.mentalsupport
            ),
            AdviceItem(
                title = "Advice 2",
                text = "Just writing some text in order to test how it will look in the card" +
                        "and making it a bit longer too so that I can test the hiding the text if its too" +
                        "long and then testing the expand button too so at this point this is just rambling" +
                        "text in order to fill the space size because I dont want more than 3 lines showing.",
                image = R.drawable.mentalsupport
            )
        ),
        onBack = {}
    )
}
