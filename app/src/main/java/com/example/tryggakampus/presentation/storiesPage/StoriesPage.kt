package com.example.tryggakampus.presentation.storiesPage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.domain.model.StoryModel

@Composable
fun StoriesPage(viewModel: StoriesPageViewModel = viewModel<StoriesPageViewModel>()) {
    LaunchedEffect(Unit) {
        viewModel.loadStories()
    }

    LazyColumn(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(viewModel.stories) { item: StoryModel ->
            StoryBox(item, onClick = {})
        }
    }
}

@Composable
fun StoryBox(story: StoryModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .padding(10.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StoryBoxHeader(story.title?: "", story.author?: "Anonymous")
        StoryBoxBody(story.content)
    }
}

@Composable
fun StoryBoxHeader(title: String, author: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 20.sp)
        Text(author, fontSize = 12.sp)
    }
}

@Composable
fun StoryBoxBody(content: String) {
    Text(content, fontSize = 16.sp)
}