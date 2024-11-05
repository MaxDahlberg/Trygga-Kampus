package com.example.tryggakampus.presentation.articlesPage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.ConnectivityObserver
import com.example.tryggakampus.NetworkConnectivityObserver
import com.example.tryggakampus.domain.model.ArticleModel

@Composable
fun ArticlesPage(viewModel: ArticlesPageViewModel = viewModel()) {
    val localContext = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var articleToDelete by remember { mutableStateOf<ArticleModel?>(null) }
    val errorMessage by viewModel.errorMessage.collectAsState()

    val connectivityObserver: ConnectivityObserver = NetworkConnectivityObserver(localContext)
    val networkStatusState = connectivityObserver
        .observe()
        .collectAsState(
            initial = ConnectivityObserver.Status.Unavailable
        )

    LaunchedEffect(Unit) {
        viewModel.loadArticles(localContext)
    }

    if (viewModel.loadingArticles.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading articles ...")
            Spacer(modifier = Modifier.size(20.dp))
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        return
    }

    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (viewModel.articles.isEmpty()) {
                    Text(
                        text = "No articles available. Please check back later or try refreshing.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        items(viewModel.articles) { item: ArticleModel ->
                            ArticleBox(
                                article = item,
                                onDelete = { articleToDelete = item },
                                onClick = {},
                                deleteMode = viewModel.deleteMode
                            )
                        }
                    }
                }

                if (networkStatusState.value == ConnectivityObserver.Status.Unavailable ||
                    networkStatusState.value == ConnectivityObserver.Status.Lost
                ) {
                    return@Scaffold
                }

                if (viewModel.deleteMode) {
                    FloatingActionButton(
                        onClick = { viewModel.toggleDeleteMode() },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .width(120.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cancel Delete Mode",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .background(Color.Transparent)
                                .width(100.dp),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Article")
                        }

                        FloatingActionButton(
                            onClick = { viewModel.toggleDeleteMode() },
                            modifier = Modifier
                                .background(Color.Transparent)
                                .width(100.dp),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Enable Delete Mode")
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddArticleDialog(
                    onDismiss = { showAddDialog = false },
                    viewModel = viewModel
                )
            }

            if (articleToDelete != null) {
                AlertDialog(
                    onDismissRequest = { articleToDelete = null },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete this article?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                articleToDelete?.let { viewModel.deleteArticle(it) }
                                articleToDelete = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { articleToDelete = null }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearErrorMessage() },
                    title = { Text("Error") },
                    text = { Text(errorMessage ?: "An unknown error occurred.") },
                    confirmButton = {
                        Button(onClick = { viewModel.clearErrorMessage() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun ArticleBox(
    article: ArticleModel,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    deleteMode: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ArticleBoxHeader(article.title ?: "")
            ArticleBoxBody(article.summary, article.webpage ?: "No Link")
        }

        if (deleteMode) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Article",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(25.dp)
                )
            }

        }
    }
}

@Composable
fun ArticleBoxHeader(title: String) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ArticleBoxBody(content: String, webpage: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(content, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
        Text(
            buildAnnotatedString {
                withLink(LinkAnnotation.Url(url = webpage)) {
                    append("Read More")
                }
            },
            color = Color(0xFFF19107),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

