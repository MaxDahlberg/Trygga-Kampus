package com.example.tryggakampus.presentation.component.customDrawer

import com.example.tryggakampus.R

enum class AccessLevel {
    AUTH_ONLY,
    UNAUTH_ONLY,
    ALL
}

enum class DrawerItem(
    val title: String,
    val icon: Int,
    val accessLevel: AccessLevel = AccessLevel.ALL
) {
    Home(
        icon = R.drawable.baseline_home_filled_24,
        title = "Home"
    ),
    Profile(
        icon = R.drawable.baseline_person_24,
        title = "Profile",
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Articles(
        icon = R.drawable.baseline_articles_24,
        title = "Articles"
    ),
    Form(
        icon = R.drawable.baseline_forum_24,
        title = "Form"
    ),
    Survey(
        icon = R.drawable.baseline_diamond_24,
        title = "Survey",
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Stories(
        icon = R.drawable.baseline_articles_24,
        title = "Stories"
    ),
    Advice(
        icon = R.drawable.baseline_advice_24,
        title = "Advice"
    ),
    Login(
        icon = R.drawable.baseline_articles_24,
        title = "Login",
        accessLevel = AccessLevel.UNAUTH_ONLY
    ),

    // BUTTONS ON BOTTOM
    Logout(
        icon = R.drawable.baseline_person_24,
        title = "Logout",
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Settings(
        icon = R.drawable.baseline_settings_24,
        title = "Settings"
    )
}