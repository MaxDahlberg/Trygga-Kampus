package com.example.tryggakampus.presentation.storiesPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tryggakampus.R
import com.example.tryggakampus.data.Config
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun NewStoryPage(modifier: Modifier = Modifier, viewModel: StoriesPageViewModel) {
    PageContainer(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(R.string.stories_page_title), fontSize = 20.sp)
        Column (modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(15.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                value = viewModel.storyTitleValue.value,
                label = { Text(stringResource(R.string.stories_title_label)) },
                onValueChange = { viewModel.setStoryTitleValue(it) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.secondary,

                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = Color.Transparent,

                    cursorColor = MaterialTheme.colorScheme.secondary,

                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                value = viewModel.storyFormValue.value,
                label = { Text(stringResource(R.string.stories_content_label)) },
                onValueChange = { viewModel.setStoryFormValue(it) },
                minLines = 5,
                maxLines = 8,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.secondary,

                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = Color.Transparent,

                    cursorColor = MaterialTheme.colorScheme.secondary,

                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textLength = viewModel.storyFormValue.value.text.length

                val color =
                    if (textLength < Config.Stories.minLength
                    || textLength > Config.Stories.maxLength)
                        Color.Red
                    else
                        Color.Unspecified

                SwitchWithIcon(stringResource(R.string.stories_anonymous_label), onToggle = {
                    viewModel.setStoryAnonymity(it)
                })

                if (textLength < Config.Stories.minLength) {
                    Text("Minimum $textLength / ${Config.Stories.minLength}", color = color)
                } else {
                    Text("Maximum $textLength / ${Config.Stories.maxLength}", color = color)
                }
            }
        }
    }
}

@Composable
fun SwitchWithIcon(label: String, onToggle: (b: Boolean) -> Unit) {
    var checked by remember { mutableStateOf(true) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(
            checked = checked,
            onCheckedChange = { checked = it; onToggle(it) },
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
            colors = SwitchDefaults.colors(
                uncheckedIconColor = MaterialTheme.colorScheme.background,
                checkedIconColor = MaterialTheme.colorScheme.secondary,
                checkedBorderColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                checkedThumbColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}