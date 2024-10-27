package com.example.tryggakampus.presentation.advicePage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.AdviceCategory

// idea is to have a "landing page" for the advice where you can pick in between 2 categories.
// you can then go back after selecting. The advice will be displayed by categories.
@Composable
fun AdvicePage() {
    var selectedCategory by remember { mutableStateOf<AdviceCategory?>(null) }

    when (selectedCategory) {
        AdviceCategory.PREVENTION -> AdviceListScreen(
            title = stringResource(id = R.string.preventive_advice_category_title),
            adviceItems = getPreventiveAdviceItems(),
            onBack = { selectedCategory = null }
        )
        AdviceCategory.SUPPORT -> AdviceListScreen(
            title = stringResource(id = R.string.support_advice_category_title),
            adviceItems = getSupportAdviceItems(),
            onBack = { selectedCategory = null }
        )
        AdviceCategory.FAMILY -> AdviceListScreen(
            title = stringResource(id = R.string.family_advice_category_title),
            adviceItems = getFamilyAdviceItems(),
            onBack = { selectedCategory = null }
        )
        null -> CategorySelectionScreen(onCategorySelected = { selectedCategory = it })
    }
}

@Composable
fun CategorySelectionScreen(onCategorySelected: (AdviceCategory) -> Unit) {
    Column {
        Text(
            text = "Select Category",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        CategoryCard(
            title = stringResource(id = R.string.preventive_advice_category_title),
            image = R.drawable.mentalsupport,
            onClick = { onCategorySelected(AdviceCategory.PREVENTION) }
        )

        CategoryCard(
            title = stringResource(id = R.string.support_advice_category_title),
            image = R.drawable.support,
            onClick = { onCategorySelected(AdviceCategory.SUPPORT) }
        )

        CategoryCard(
            title = stringResource(id = R.string.family_advice_category_title),
            image = R.drawable.familysupport,
            onClick = { onCategorySelected(AdviceCategory.FAMILY) }
        )
    }
}


