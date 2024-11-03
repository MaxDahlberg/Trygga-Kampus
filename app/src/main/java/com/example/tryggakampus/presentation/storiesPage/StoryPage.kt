package com.example.tryggakampus.presentation.storiesPage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun StoryPage(viewModel: StoriesPageViewModel, storyModelId: String) {
    val story = remember { mutableStateOf(
        viewModel.stories.find { it.id == storyModelId }
        ?: StoryModel()
    )}

    val navigator = LocalNavController.current
    BackHandler {
        navigator.navigateUp()
    }

    PageContainer (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
    ) {
        StoryBox(story.value, onClick = {})
    }
}