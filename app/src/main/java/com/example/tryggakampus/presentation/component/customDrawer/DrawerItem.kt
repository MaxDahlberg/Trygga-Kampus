package com.example.tryggakampus.presentation.component.customDrawer

import com.example.tryggakampus.R

enum class AccessLevel {
    AUTH_ONLY,
    UNAUTH_ONLY,
    ALL
}

enum class DrawerItem(
    val titleResId: Int, // Change from String to resource ID
    val icon: Int,
    val accessLevel: AccessLevel = AccessLevel.ALL
) {
    Home(
        icon = R.drawable.baseline_home_filled_24,
        titleResId = R.string.drawer_home
    ),
    Profile(
        icon = R.drawable.baseline_person_24,
        titleResId = R.string.drawer_profile,
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Articles(
        icon = R.drawable.baseline_articles_24,
        titleResId = R.string.drawer_articles
    ),
    Form(
        icon = R.drawable.baseline_forum_24,
        titleResId = R.string.drawer_form
    ),
    Survey(
        icon = R.drawable.baseline_diamond_24,
        titleResId = R.string.drawer_survey,
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Habits(
        icon = R.drawable.baseline_check_circle_24,
        titleResId = R.string.drawer_habits,
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Stories(
        icon = R.drawable.baseline_articles_24,
        titleResId = R.string.drawer_stories
    ),
    Videos(
        icon = R.drawable.baseline_articles_24,
        title = "Videos"
    ),
    Advice(
        icon = R.drawable.baseline_advice_24,
        titleResId = R.string.drawer_advice
    ),
    Voice(
        icon = R.drawable.baseline_articles_24,
        title = "Voice Note"
    ),
    Game(
        icon = R.drawable.baseline_diamond_24,
        title = "Game"
    ),
    Login(
        icon = R.drawable.baseline_articles_24,
        titleResId = R.string.drawer_login,
        accessLevel = AccessLevel.UNAUTH_ONLY
    ),
    Logout(
        icon = R.drawable.baseline_person_24,
        titleResId = R.string.drawer_logout,
        accessLevel = AccessLevel.AUTH_ONLY
    ),
    Settings(
        icon = R.drawable.baseline_settings_24,
        titleResId = R.string.drawer_settings
    )
}