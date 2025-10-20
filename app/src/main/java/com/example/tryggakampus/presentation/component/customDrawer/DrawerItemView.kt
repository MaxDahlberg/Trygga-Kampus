package com.example.tryggakampus.presentation.component.customDrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationItemView(
    drawerItem: DrawerItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedBg = MaterialTheme.colorScheme.secondary
    val unselectedBg = MaterialTheme.colorScheme.surfaceVariant
    val selectedFg = MaterialTheme.colorScheme.onSecondary
    val unselectedFg = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 99.dp))
            .clickable { onClick() }
            .background(color = if (selected) selectedBg else unselectedBg, shape = RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = drawerItem.icon),
            contentDescription = "Navigation Item Icon",
            tint = if (selected) selectedFg else unselectedFg
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = drawerItem.title,
            color = if (selected) selectedFg else unselectedFg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
}