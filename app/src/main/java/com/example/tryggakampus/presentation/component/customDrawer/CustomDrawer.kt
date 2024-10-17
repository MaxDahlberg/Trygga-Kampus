package com.example.tryggakampus.presentation.component.customDrawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.R
import com.example.tryggakampus.Routes

@Composable
fun CustomDrawer(
    selectedDrawerItem: DrawerItem,
    onNavigationItemClick: (DrawerItem) -> Unit,
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.6f)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val drawerItems: Array<DrawerItem> = DrawerItem.entries.toTypedArray()

        Column {
            DrawerActionBar(onCloseClick = onCloseClick)
            AppLogo()
            PrimaryDrawerItems(
                drawerItems = drawerItems.take(5),
                selectedItem = selectedDrawerItem,
                onClickItem = onNavigationItemClick
            )
        }

        SecondaryDrawerItems(
            drawerItems = drawerItems.takeLast(1),
            selectedItem = selectedDrawerItem,
            onClickItem = onNavigationItemClick
        )
    }
}

@Composable
fun AppLogo() {
    Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Image(
            modifier = Modifier.size(100.dp),
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            alignment = Alignment.Center
        )
    }

    Spacer(modifier = Modifier.height(40.dp))
}


@Composable
fun DrawerActionBar(onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Sharp.Close,
                contentDescription = "Back Arrow Icon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}


@Composable
fun PrimaryDrawerItems(
    drawerItems: List<DrawerItem>,
    selectedItem: DrawerItem,
    onClickItem: (DrawerItem) -> Unit
) {
    val navController = LocalNavController.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        drawerItems.forEach { drawerItem ->
            NavigationItemView(
                drawerItem = drawerItem,
                selected = drawerItem == selectedItem,
                onClick = {
                    onClickItem(drawerItem)
                    navController.navigate(when(drawerItem) {
                        DrawerItem.Home -> Routes.LandingPage()
                        DrawerItem.Profile -> Routes.ProfilePage()
                        DrawerItem.Articles -> Routes.ArticlesPage()
                        DrawerItem.Forum -> Routes.FormPage()
                        DrawerItem.Survey -> Routes.SurveyPage()
                        DrawerItem.Stories -> Routes.StoriesPage()
                        else -> {}
                    })
                }
            )
        }
    }
}


@Composable
fun SecondaryDrawerItems(
    drawerItems: List<DrawerItem>,
    selectedItem: DrawerItem,
    onClickItem: (DrawerItem) -> Unit
) {
    val navController = LocalNavController.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        drawerItems.forEach { drawerItem ->
            NavigationItemView(
                drawerItem = drawerItem,
                selected = drawerItem == selectedItem,
                onClick = {
                    onClickItem(drawerItem)
                    navController.navigate(when(drawerItem) {
                        DrawerItem.Settings -> Routes.SettingsPage()
                        else -> {}
                    })
                }
            )
        }
    }
}