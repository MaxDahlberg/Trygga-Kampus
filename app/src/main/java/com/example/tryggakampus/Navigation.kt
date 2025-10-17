package com.example.tryggakampus

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
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
import com.example.tryggakampus.presentation.formPage.FormPage
import com.example.tryggakampus.presentation.advicePage.AdvicePage
import com.example.tryggakampus.presentation.surveyPage.SurveyPage
import com.example.tryggakampus.presentation.authentication.loginPage.LoginPage
import com.example.tryggakampus.presentation.authentication.registerPage.RegisterPage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

val LocalNavController = compositionLocalOf<NavHostController> {
    error("NavController not provided")
}

sealed interface Routes {
    fun routeName(): String

    @kotlinx.serialization.Serializable
    data class LandingPage(val title: String = "Home"): Routes {
        override fun routeName() = "LandingPage"
    }

    @kotlinx.serialization.Serializable
    data class SettingsPage(val title: String = "Settings"): Routes {
        override fun routeName() = "SettingsPage"
    }

    @kotlinx.serialization.Serializable
    data class ProfilePage(val title: String = "Profile"): Routes {
        override fun routeName() = "ProfilePage"
    }

    @kotlinx.serialization.Serializable
    data class ArticlesPage(val title: String = "Articles"): Routes {
        override fun routeName() = "ArticlesPage"
    }

    @kotlinx.serialization.Serializable
    data class FormPage(val title: String = "Form"): Routes {
        override fun routeName() = "FormPage"
    }

    // STORIES SUB-GRAPH (unchanged)
    @kotlinx.serialization.Serializable
    object StoriesNavGraph {
        @kotlinx.serialization.Serializable
        data object StoriesPage: Routes {
            override fun routeName() = "StoriesPage"
        }
        @kotlinx.serialization.Serializable
        data class StoryPage(val storyModelId: String = "n07f0und"): Routes {
            override fun routeName() = "StoryPage"
        }
    }

    @kotlinx.serialization.Serializable
    data class AdvicePage(val title: String = "Advice"): Routes {
        override fun routeName() = "AdvicePage"
    }

    @kotlinx.serialization.Serializable
    data class SurveyPage(val title: String = "Survey"): Routes {
        override fun routeName() = "SurveyPage"
    }

    // ✅ NEW: SoberActivity is a NORMAL top-level page (NOT inside Authentication)
    @kotlinx.serialization.Serializable
    data class SoberActivityPage(val title: String = "Sober Activity"): Routes {
        override fun routeName() = "SoberActivityPage"
    }

    // AUTH SUB-GRAPH (unchanged)
    @kotlinx.serialization.Serializable
    data object Authentication {
        @kotlinx.serialization.Serializable
        data object LoginPage: Routes {
            override fun routeName() = "LoginPage"
        }
        @kotlinx.serialization.Serializable
        data object RegisterPage: Routes {
            override fun routeName() = "RegisterPage"
        }
    }
}


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun Navigation(
    children: @Composable() (page: @Composable()() -> Unit) -> Unit
) {
    val navController = rememberNavController()

    observeAuthStateChanges(navController)

    CompositionLocalProvider(LocalNavController provides navController) {
        children {
            NavHost(navController = navController, startDestination = Routes.LandingPage()) {
                composable<Routes.LandingPage> {
                    val args = it.toRoute<Routes.LandingPage>()
                    LandingPage(args.title)
                }

                composable<Routes.ProfilePage> { ProfilePage() }
                composable<Routes.ArticlesPage> { ArticlesPage() }

                composable<Routes.FormPage> {
                    val args = it.toRoute<Routes.FormPage>()
                    FormPage(args.title)
                }

                composable<Routes.SurveyPage> {
                    val args = it.toRoute<Routes.SurveyPage>()
                    SurveyPage(args.title)
                }

                // Stories subgraph (unchanged) …

                composable<Routes.AdvicePage> { AdvicePage() }

                composable<Routes.SettingsPage> {
                    val args = it.toRoute<Routes.SettingsPage>()
                    val vm = viewModel<com.example.tryggakampus.presentation.settingsPage.SettingsPageViewModel>()
                    SettingsPage(vm, args.title)
                }

                // ✅ Register SoberActivity as a normal page
                composable<Routes.SoberActivityPage> {
                    val args = it.toRoute<Routes.SoberActivityPage>()
                    com.example.tryggakampus.presentation.sober.SoberActivityPage(args.title)
                }

                // Authentication subgraph (unchanged)
                navigation<Routes.Authentication>(startDestination = Routes.Authentication.LoginPage) {
                    composable<Routes.Authentication.LoginPage> {
                        LoginPage()
                    }
                    composable<Routes.Authentication.RegisterPage> {
                        RegisterPage()
                    }
                }
            }

        }
    }
}

fun observeAuthStateChanges(navController: NavHostController) {
    Firebase.auth.addAuthStateListener { auth ->
        handleAuthStateChange(auth.currentUser != null, navController)
    }
}

private fun handleAuthStateChange(isAuthenticated: Boolean, navController: NavHostController) {
    val lifecycle = navController.currentBackStackEntry?.lifecycle ?: return

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_START) {
                navigateBasedOnAuthState(isAuthenticated, navController)
                lifecycle.removeObserver(this)
            }
        }
    }

    lifecycle.addObserver(observer)
}

private fun navigateBasedOnAuthState(isAuthenticated: Boolean, navController: NavHostController) {
    if (isAuthenticated) {
        Log.d("Auth", "User authenticated, redirecting to Landing page")
        navController.navigate(Routes.LandingPage()) {
            popUpTo(0) { inclusive = true }
        }
    } else {
        Log.d("Auth", "User logged out, redirecting to Login page")
        navController.navigate(Routes.Authentication.LoginPage) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }
}
