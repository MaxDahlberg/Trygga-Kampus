package com.example.tryggakampus.presentation.storiesPage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.domain.model.StoryCommentModel
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.presentation.component.ErrorBox
import com.example.tryggakampus.presentation.component.PageContainer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun StoryPage(viewModel: StoriesPageViewModel, storyModelId: String) {
    val story = remember {
        mutableStateOf(
            viewModel.stories.find { it.id == storyModelId } ?: StoryModel()
        )
    }

    val navigator = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    // Load comments automatically when this page is opened
    LaunchedEffect(storyModelId) {
        viewModel.loadComments(storyModelId)
    }

    BackHandler { navigator.navigateUp() }

    PageContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
                    SwitchWithIcon(
                        label = "Anonymous",
                        checked = viewModel.commentAnonymity.value,
                        onToggle = { viewModel.setCommentAnonymity(it) }
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.postComment(storyModelId)
                            }
                        },
                        enabled = viewModel.commentText.value.text.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Comments error display
            viewModel.commentError.value?.let { errorMessage ->
                ErrorBox(message = errorMessage) { viewModel.clearCommentError() }
            }

            // Scrollable comments section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (viewModel.comments.isEmpty()) {
                    Text(
                        "No comments yet. Be the first to comment!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.comments.forEach { comment ->
                            StoryCommentBox(
                                comment = comment,
                                onDelete = { viewModel.deleteComment(comment) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCommentBox(
    comment: StoryCommentModel,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top left: Username
                Text(
                    text = if (comment.anonymous) "Anonymous" else (comment.author ?: "Unknown User"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Top right: Delete if current user is author
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser?.uid == comment.userId) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Red,
                        modifier = Modifier
                            .clickable { onDelete() }
                            .padding(top = 0.dp)
                    )
                }
            }

            // Comment body
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
