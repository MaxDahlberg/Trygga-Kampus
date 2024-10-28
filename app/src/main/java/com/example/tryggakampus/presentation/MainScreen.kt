package com.example.tryggakampus.presentation

import AppBar
import android.annotation.SuppressLint

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.tryggakampus.presentation.component.BottomAppBar
import com.example.tryggakampus.presentation.component.customDrawer.CustomDrawer

import com.example.tryggakampus.presentation.component.customDrawer.CustomDrawerState
import com.example.tryggakampus.presentation.component.customDrawer.DrawerItem
import com.example.tryggakampus.presentation.component.customDrawer.isOpened
import com.example.tryggakampus.presentation.component.customDrawer.opposite
import com.example.tryggakampus.presentation.util.coloredShadow

import kotlin.math.roundToInt

@Composable
fun MainScreen(
    page: @Composable() () -> Unit
) {
    val (drawerState, setDrawerState) = remember { mutableStateOf(CustomDrawerState.Closed) }
    val (selectedNavItem, setSelectedNavItem) = remember { mutableStateOf(DrawerItem.Home) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density

    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * density).roundToInt() }
    }
    val offsetValue = remember { derivedStateOf { (screenWidth.value / 4.5).dp } }

    val animatedOffset by animateDpAsState(
        targetValue =
            if (drawerState.isOpened())
                offsetValue.value
            else
                0.dp,
        label = "Animated Offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue =
            if (drawerState.isOpened())
                0.9f
            else
                1f,
        label = "Animated Scale"
    )

    BackHandler(enabled = drawerState.isOpened()) {
        setDrawerState(CustomDrawerState.Closed)
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = {change, dragAmount ->
                        change.consume()

                        if (dragAmount >= 40)  setDrawerState(CustomDrawerState.Opened)
                        if (dragAmount <= -40) setDrawerState(CustomDrawerState.Closed)
                    }
                )
            }
    ) {
        CustomDrawer(
            selectedDrawerItem = selectedNavItem,
            onNavigationItemClick = {
                setSelectedNavItem(it)
                setDrawerState(CustomDrawerState.Closed)
            },
            onCloseClick = { setDrawerState(CustomDrawerState.Closed) }
        )

        MainContent(
            modifier = Modifier
                .offset(x = animatedOffset)
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = Color.Black,
                    alpha = 0.1f,
                    shadowRadius = 50.dp
                ),
            drawerState = drawerState,
            onDrawerClick = { setDrawerState(it) },
            page = page
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit,
    page: @Composable() () -> Unit
) {
    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                onDrawerClick(CustomDrawerState.Closed)
            },
        topBar = {
            AppBar(onNavigationIconClick = {
                onDrawerClick(drawerState.opposite())
            })
        },
        bottomBar = {
            BottomAppBar()
        }
    ) { padding ->
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                page()
            }

            if (drawerState == CustomDrawerState.Opened) {
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.08f))
                        .clickable {
                            onDrawerClick(CustomDrawerState.Closed)
                        },
                    content = {}
                )
            }
        }
    }
}
