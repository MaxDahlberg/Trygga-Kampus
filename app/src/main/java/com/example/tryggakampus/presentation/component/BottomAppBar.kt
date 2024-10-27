package com.example.tryggakampus.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.currentBackStackEntryAsState

import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.Routes
import com.example.tryggakampus.presentation.settingsPage.ArticleTabs
import com.example.tryggakampus.presentation.settingsPage.SettingsPageViewModel

@Composable
fun BottomAppBar() {
    val navigator = LocalNavController.current
    val currentRoute = navigator.currentBackStackEntryAsState().value?.destination?.route
        ?: return

    val className = currentRoute
        .substringBefore("?")
        .substringAfterLast(".")

    when (className) {
        Routes.SettingsPage().routeName() -> BottomSettingsBar()
        // Routes.ArticlesPage().routeName() -> BottomAppBar { BottomArticlesBar() }
        // Routes.LandingPage().routeName() -> BottomAppBar { BottomLandingBar() }
        // Routes.ProfilePage().routeName() -> BottomAppBar { BottomProfileBar() }
        else -> {}
    }
}


@Composable
fun BottomSettingsBar() {
    val navController = LocalNavController.current
    val navigationGraphEntry = remember {
        navController.getBackStackEntry<Routes.SettingsPage>()
    }
    val vm = viewModel<SettingsPageViewModel>(navigationGraphEntry)

    BottomAppBar(
        modifier = Modifier
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(10.dp)),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(
                onClick = { vm.setTabIndex(ArticleTabs.TAB_ONE) },
                // temporary color for an active state:
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "general info"
                )
            }
            IconButton(onClick = { vm.setTabIndex(ArticleTabs.TAB_TWO) }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "articles about health"
                )
            }
            IconButton(onClick = { vm.setTabIndex(ArticleTabs.TAB_THREE) }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "location"
                )
            }
        }
    }
}

@Composable
fun BottomLandingBar() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "menu"
            )
        }
    }
}

@Composable
fun BottomProfileBar() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "menu"
            )
        }
    }
}
