package com.example.tryggakampus.presentation.storiesPage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.domain.model.StoryCommentModel
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun StoryPage(viewModel: StoriesPageViewModel, storyModelId: String) {
    val story = remember {
        mutableStateOf(
            viewModel.stories.find { it.id == storyModelId } ?: StoryModel()
        )
    }

    val navigator = LocalNavController.current
    BackHandler { navigator.navigateUp() }

    PageContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        enableScroll = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Story content
            StoryBox(
                story = story.value,
                onDelete = {
                    viewModel.deleteStory(story.value)
                    navigator.navigateUp()
                           },
                onCommentClick = {},
                showCommentButton = false
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Comment input section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    value = viewModel.commentText.value,
                    label = { Text("Write a comment...") },
                    onValueChange = { viewModel.setCommentText(it) },
                    singleLine = false,
                    maxLines = 5,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SwitchWithIcon("Post anonymously") {
                        viewModel.setCommentAnonymity(it)
                    }

                    Button(
                        onClick = { /* viewModel.postComment(storyModelId) */ },
                        enabled = viewModel.commentText.value.text.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Comments list
            if (viewModel.comments.isEmpty()) {
                Text(
                    "No comments yet. Be the first to comment!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.comments) { comment ->
                        StoryCommentBox(comment)
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCommentBox(comment: StoryCommentModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(10.dp)
    ) {
        Text(
            text = if (comment.anonymous) "Anonymous" else (comment.author ?: "Unknown User"),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}