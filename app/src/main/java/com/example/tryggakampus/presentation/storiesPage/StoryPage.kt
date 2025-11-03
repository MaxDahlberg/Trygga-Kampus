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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.domain.model.StoryCommentModel
import com.example.tryggakampus.domain.model.StoryModel
import com.example.tryggakampus.presentation.component.ErrorBox
import com.example.tryggakampus.presentation.component.PageContainer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.tryggakampus.R

@Composable
fun StoryPage(viewModel: StoriesPageViewModel, storyModelId: String) {
    val context = LocalContext.current

    val story = remember {
        mutableStateOf(
            viewModel.stories.find { it.id == storyModelId } ?: StoryModel()
        )
    }

    val navigator = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(storyModelId) {
        viewModel.loadComments(context, storyModelId)
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
                    label = { Text(stringResource(R.string.write_a_comment)) },
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
                        label = stringResource(R.string.stories_anonymous_label),
                        checked = viewModel.commentAnonymity.value,
                        onToggle = { viewModel.setCommentAnonymity(it) }
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.postComment(context, storyModelId)
                            }
                        },
                        enabled = viewModel.commentText.value.text.isNotBlank()
                    ) {
                        Text(stringResource(R.string.post))
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
                        stringResource(R.string.no_comments),
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
                                onDelete = { viewModel.deleteComment(context, comment) }
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
                val authorText = when {
                    comment.anonymous -> stringResource(R.string.stories_anonymous_label)
                    comment.author.isNullOrBlank() -> stringResource(R.string.unknown_user)
                    else -> comment.author
                }

                Text(
                    text = authorText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Top right: Delete if current user is author
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser?.uid == comment.userId) {
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Red,
                        modifier = Modifier
                            .clickable { onDelete() }
                            .padding(top = 0.dp)
                    )
                }
            }

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
