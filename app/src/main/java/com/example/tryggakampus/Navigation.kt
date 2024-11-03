package com.example.tryggakampus

import androidx.compose.animation.AnimatedContentTransitionScope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute

import com.example.tryggakampus.presentation.landingPage.LandingPage
import com.example.tryggakampus.presentation.profilePage.ProfilePage
import com.example.tryggakampus.presentation.settingsPage.SettingsPage
import com.example.tryggakampus.presentation.articlesPage.ArticlesPage
import com.example.tryggakampus.presentation.settingsPage.SettingsPageViewModel
import com.example.tryggakampus.presentation.formPage.FormPage
import com.example.tryggakampus.presentation.storiesPage.StoriesPage
import com.example.tryggakampus.presentation.storiesPage.StoriesPageViewModel
import com.example.tryggakampus.presentation.storiesPage.StoryPage
import com.example.tryggakampus.presentation.advicePage.AdvicePage
import com.example.tryggakampus.presentation.surveyPage.SurveyPage

import kotlinx.serialization.Serializable

val LocalNavController = compositionLocalOf<NavHostController> {
    error("NavController not provided")
}

sealed interface Routes {
    fun routeName(): String

    @Serializable data class LandingPage(val title: String = "Home"): Routes {
        override fun routeName() = "LandingPage"
    }

    @Serializable data class SettingsPage(val title: String = "Settings"): Routes {
        override fun routeName() = "SettingsPage"
    }

    @Serializable data class ProfilePage(val title: String = "Profile"): Routes {
        override fun routeName() = "ProfilePage"
    }

    @Serializable data class ArticlesPage(val title: String = "Articles"): Routes {
        override fun routeName() = "ArticlesPage"
    }

    @Serializable  data class FormPage(val title: String = "Form"): Routes {
        override fun routeName() = "FormPage"
    }

    @Serializable object StoriesNavGraph {
        @Serializable data object StoriesPage: Routes {
            override fun routeName() = "StoriesPage"
        }

        @Serializable data class StoryPage(val storyModelId: String = "n07f0und"): Routes {
            override fun routeName() = "StoryPage"
        }
    }

    @Serializable data class AdvicePage(val title: String = "Advice"): Routes {
        override fun routeName() = "AdvicePage"
    }

    @Serializable data class SurveyPage(val title: String = "Survey"): Routes {
        override fun routeName() = "SurveyPage"
    }

}

@Composable
fun Navigation(
    children: @Composable() (page: @Composable()() -> Unit) -> Unit
) {
    val navController = rememberNavController()

    /*
    *   The reason for adding CompositionLocalProvider is to avoid prop-drilling,
    *   which is a problem with frameworks like react(, and jetpack compose)
    *   https://www.freecodecamp.org/news/prop-drilling-in-react-explained-with-examples/
    *
    *   solution from:
    *   https://medium.com/@ramadan123sayed/composition-local-in-jetpack-compose-4d0a54afa67c#36f0
    * */
    CompositionLocalProvider(LocalNavController provides navController) {
        children {
            NavHost(navController = navController, startDestination = Routes.LandingPage()) {
                composable<Routes.LandingPage> {
                    val args = it.toRoute<Routes.LandingPage>()
                    LandingPage(args.title)
                }

                composable<Routes.ProfilePage> {
                    val args = it.toRoute<Routes.ProfilePage>()
                    ProfilePage(args.title)
                }

                composable<Routes.ArticlesPage> {
                    ArticlesPage()
                }

                composable<Routes.FormPage> {
                    val args = it.toRoute<Routes.FormPage>()
                    FormPage(args.title)
                }

                composable<Routes.SurveyPage> {
                    val args = it.toRoute<Routes.SurveyPage>()
                    SurveyPage(args.title)
                }

                navigation<Routes.StoriesNavGraph> (startDestination = Routes.StoriesNavGraph.StoriesPage) {
                    composable<Routes.StoriesNavGraph.StoriesPage> (
                        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right) },
                        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left) }
                    ) {
                        val vm: StoriesPageViewModel = viewModel<StoriesPageViewModel>()
                        StoriesPage(vm)
                    }

                    composable<Routes.StoriesNavGraph.StoryPage> (
                        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left) },
                        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right) },
                    ) {
                        val storiesBackStackEntry = remember { navController.getBackStackEntry(Routes.StoriesNavGraph.StoriesPage) }
                        val vm: StoriesPageViewModel = viewModel(storiesBackStackEntry)
                        val args = it.toRoute<Routes.StoriesNavGraph.StoryPage>()
                        StoryPage(vm, args.storyModelId)
                    }
                }

                composable<Routes.AdvicePage> {
                    AdvicePage()
                }

                composable<Routes.SettingsPage> {
                    val args = it.toRoute<Routes.SettingsPage>()
                    val vm = viewModel<SettingsPageViewModel>()
                    SettingsPage(vm, args.title)
                }
            }
        }
    }
}
