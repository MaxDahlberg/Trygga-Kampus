package com.example.tryggakampus.presentation.articlesPage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tryggakampus.R
import com.example.tryggakampus.domain.model.ArticleModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticlesPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingStateShowsSpinnerAndText() {
        val loadingViewModel = TestArticlesPageViewModel().apply {
            setLoadingForTest(true)
        }

        composeTestRule.setContent {
            ArticlesPage(viewModel = loadingViewModel)
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.loading_articles)).assertExists()
        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun emptyStateShowsNoArticlesMessage() {
        val emptyViewModel = TestArticlesPageViewModel().apply {
            setLoadingForTest(false)
            clearArticlesForTest()
        }

        composeTestRule.setContent {
            ArticlesPage(viewModel = emptyViewModel)
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.no_articles_available)).assertExists()
    }

    @Test
    fun loadedArticlesRenderInLazyColumnWithTitlesAndLinks() {
        val loadedViewModel = TestArticlesPageViewModel()
        loadedViewModel.loadArticles(composeTestRule.activity)
        composeTestRule.waitForIdle()

        composeTestRule.setContent {
            ArticlesPage(viewModel = loadedViewModel)
        }

        // Wait for render (simple public existence check)
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            try {
                composeTestRule.onNodeWithText("Test Article 1").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Test Article 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Summary 1").assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.read_more)).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.add_article)).assertExists()
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.enable_delete_mode)).assertExists()
    }

    @Test
    fun toggleDeleteModeShowsCloseFabAndDeleteIconsOnItems() {
        val viewModel = TestArticlesPageViewModel().apply {
            setArticlesForTest(listOf(ArticleModel(id = "1", title = "Test", summary = "", webpage = "")))
        }

        composeTestRule.setContent {
            ArticlesPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.add_article)).assertExists()

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.enable_delete_mode)).performClick()

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.cancel_delete_mode)).assertExists()
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.delete_article)).assertExists()
    }

    @Test
    fun deleteArticleFlowOpensDialogAndRemovesItem() {
        val viewModel = TestArticlesPageViewModel().apply {
            setArticlesForTest(listOf(ArticleModel(id = "1", title = "To Delete", summary = "", webpage = "")))
        }

        composeTestRule.setContent {
            ArticlesPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.enable_delete_mode)).performClick()

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.delete_article)).performClick()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.confirm_deletion)).assertExists()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.delete_confirmation)).assertExists()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.delete)).performClick()

        composeTestRule.onNodeWithText("To Delete").assertDoesNotExist()
        composeTestRule.waitForIdle()
    }

    @Test
    fun addArticleOpensDialog() {
        val viewModel = TestArticlesPageViewModel()

        composeTestRule.setContent {
            ArticlesPage(viewModel = viewModel)
        }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.add_article)).performClick()

        composeTestRule.onNodeWithText("Add Article").assertExists()
    }

    @Test
    fun errorStateShowsDialogWithMessage() {
        val errorViewModel = TestArticlesPageViewModel().apply {
            setErrorForTest("Test error")
        }

        composeTestRule.setContent {
            ArticlesPage(viewModel = errorViewModel)
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.error)).assertExists()
        composeTestRule.onNodeWithText("Test error").assertExists()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.ok)).performClick()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.error)).assertDoesNotExist()
    }

    @Test
    fun offlineNetworkHidesFabsAndShowsContentFromCache() {
        val offlineViewModel = TestArticlesPageViewModel().apply {
            setArticlesForTest(listOf(ArticleModel(title = "Cached Article", summary = "", webpage = "")))
        }

        composeTestRule.setContent {
            ArticlesPage(viewModel = offlineViewModel)
        }

        composeTestRule.onNodeWithText("Cached Article").assertExists()

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.add_article)).assertDoesNotExist()
    }
}