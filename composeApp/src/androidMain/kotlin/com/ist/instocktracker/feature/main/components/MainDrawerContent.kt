package com.ist.instocktracker.feature.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ist.instocktracker.NavDark

// Drawer dimensions / colors taken from the Figma "Menu Sidebar" design.
private val DrawerWidth = 280.dp
private val DrawerLogoutContent = Color.White.copy(alpha = 0.55f)

/**
 * The contents of the main navigation drawer (the Figma "Menu Sidebar"):
 * a navy sheet with a header, the primary navigation items, and a log-out
 * action pinned to the bottom.
 *
 * Pure UI — each click callback is expected to handle closing the drawer and
 * performing the navigation/action itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawerContent(
    onDashboardClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(DrawerWidth),
        drawerShape = RectangleShape,
        drawerContainerColor = NavDark,
        drawerContentColor = Color.White
    ) {
        // Header
        Text(
            text = "InStock Tracker",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp)
        )

        // Navigation items
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrawerItem(
                icon = Icons.Filled.Dashboard,
                label = "Dashboard",
                selected = true,
                onClick = onDashboardClick
            )
            DrawerItem(
                icon = Icons.Filled.Bolt,
                label = "Subscription",
                selected = false,
                onClick = onSubscribeClick
            )
            DrawerItem(
                icon = Icons.Filled.Notifications,
                label = "Notifications",
                selected = false,
                onClick = onNotificationsClick
            )
            DrawerItem(
                icon = Icons.Filled.Settings,
                label = "Settings",
                selected = false,
                onClick = onSettingsClick
            )
        }

        // Log out pinned to the bottom
        Box(modifier = Modifier.padding(12.dp)) {
            DrawerItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = "Log out",
                selected = false,
                contentColor = DrawerLogoutContent,
                onClick = onLogoutClick
            )
        }
    }
}
