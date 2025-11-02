package com.example.tryggakampus.presentation.articlesPage

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.domain.model.ArticleModel
import com.example.tryggakampus.domain.repository.ArticleRepository
import com.google.firebase.firestore.Source
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestArticlesPageViewModel(
    private val repo: ArticleRepository = TestArticleRepository()
) : ArticlesPageViewModel() {

    override var articles = mutableStateListOf<ArticleModel>()

    override var deleteMode: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var loadingArticles = mutableStateOf(false)

    override fun loadArticles(context: Context) {
        viewModelScope.launch {
            loadingArticles.value = true
            delay(500)  // Simulate async
            val (_, loadedArticles) = repo.getAllArticles(Source.CACHE)
            articles.clear()
            articles.addAll(loadedArticles)
            errorMessage.value = null
            loadingArticles.value = false
        }
    }

    override fun deleteArticle(article: ArticleModel) {
        viewModelScope.launch {
            repo.deleteArticle(article.id)
            articles.removeIf { it.id == article.id }
            errorMessage.value = null
        }
    }

    fun setErrorForTest(message: String?) {
        errorMessage.value = message
    }

    fun clearArticlesForTest() {
        articles.clear()
    }

    fun setArticlesForTest(newArticles: List<ArticleModel>) {
        articles.clear()
        articles.addAll(newArticles)
    }

    fun setLoadingForTest(isLoading: Boolean) {
        loadingArticles.value = isLoading
    }
}

// Fake repo
class TestArticleRepository : ArticleRepository {
    override suspend fun getAllArticles(source: Source): Pair<ArticleRepository.RepositoryResult, List<ArticleModel>> {
        return ArticleRepository.RepositoryResult.SUCCESS to listOf(
            ArticleModel(id = "1", title = "Test Article 1", summary = "Summary 1", webpage = "https://example.com/1"),
            ArticleModel(id = "2", title = "Test Article 2", summary = "Summary 2", webpage = "https://example.com/2")
        )
    }

    override suspend fun addArticle(article: ArticleModel): Pair<ArticleRepository.RepositoryResult, ArticleModel?> {
        return ArticleRepository.RepositoryResult.SUCCESS to article.copy(id = "new-id")
    }

    override suspend fun deleteArticle(articleId: String): ArticleRepository.RepositoryResult {
        return ArticleRepository.RepositoryResult.SUCCESS
    }

    override suspend fun fetchAll(source: Source): com.google.firebase.firestore.QuerySnapshot? {
        return null
    }
}