package com.example.tryggakampus.presentation.advicePage

import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.AdviceItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun getPreventiveAdviceItems(): List<AdviceItem> {
    return listOf(
        AdviceItem(
            title = stringResource(id = R.string.preventive_advice_title_1),
            text = stringResource(id = R.string.preventive_advice_text_1),
            image = R.drawable.mentalsupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.preventive_advice_title_2),
            text = stringResource(id = R.string.preventive_advice_text_2),
            image = R.drawable.mentalsupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.preventive_advice_title_3),
            text = stringResource(id = R.string.preventive_advice_text_3),
            image = R.drawable.mentalsupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.preventive_advice_title_4),
            text = stringResource(id = R.string.preventive_advice_text_4),
            image = R.drawable.mentalsupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.preventive_advice_title_5),
            text = stringResource(id = R.string.preventive_advice_text_5),
            image = R.drawable.mentalsupport
        )
    )
}

@Composable
fun getSupportAdviceItems(): List<AdviceItem> {
    return listOf(
        AdviceItem(
            title = stringResource(id = R.string.support_advice_title_1),
            text = stringResource(id = R.string.support_advice_text_1),
            image = R.drawable.support
        ),
        AdviceItem(
            title = stringResource(id = R.string.support_advice_title_2),
            text = stringResource(id = R.string.support_advice_text_2),
            image = R.drawable.support
        ),
        AdviceItem(
            title = stringResource(id = R.string.support_advice_title_3),
            text = stringResource(id = R.string.support_advice_text_3),
            image = R.drawable.support
        ),
        AdviceItem(
            title = stringResource(id = R.string.support_advice_title_4),
            text = stringResource(id = R.string.support_advice_text_4),
            image = R.drawable.support
        ),
        AdviceItem(
            title = stringResource(id = R.string.support_advice_title_5),
            text = stringResource(id = R.string.support_advice_text_5),
            image = R.drawable.support
        )
    )
}

@Composable
fun getFamilyAdviceItems(): List<AdviceItem> {
    return listOf(
        AdviceItem(
            title = stringResource(id = R.string.family_advice_title_1),
            text = stringResource(id = R.string.family_advice_text_1),
            image = R.drawable.familysupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.family_advice_title_2),
            text = stringResource(id = R.string.family_advice_text_2),
            image = R.drawable.familysupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.family_advice_title_3),
            text = stringResource(id = R.string.family_advice_text_3),
            image = R.drawable.familysupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.family_advice_title_4),
            text = stringResource(id = R.string.preventive_advice_text_4),
            image = R.drawable.familysupport
        ),
        AdviceItem(
            title = stringResource(id = R.string.family_advice_title_5),
            text = stringResource(id = R.string.family_advice_text_5),
            image = R.drawable.familysupport
        )
    )
}