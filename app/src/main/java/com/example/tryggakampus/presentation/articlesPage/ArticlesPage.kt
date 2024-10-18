package com.example.tryggakampus.presentation.articlesPage

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.domain.model.ArticleModel

@Composable
fun ArticlesPage(viewModel: ArticlesPageViewModel = viewModel<ArticlesPageViewModel>()) {
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadArticles(localContext)
    }

    LazyColumn(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(viewModel.articles) { item: ArticleModel ->
            ArticleBox(item, onClick = {})
        }
    }
}

@Composable
fun ArticleBox(article: ArticleModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .padding(10.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
         ArticleBoxHeader(article.title?: "")
         ArticleBoxBody(article.summary, article.webpage?: "No Link")
    }
}

@Composable
fun ArticleBoxHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArticleBoxBody(content: String, webpage: String) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(content, fontSize = 16.sp)

        ClickableText(
            text = AnnotatedString("Read More"),
            style = TextStyle(
                color = Color(0xFFF19107),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webpage))
                context.startActivity(intent)
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

