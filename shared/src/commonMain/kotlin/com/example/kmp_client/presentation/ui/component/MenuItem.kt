package com.example.kmp_client.presentation.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.kmp_client.presentation.navigation.NavRoutes
import com.example.kmp_client.core.exitApp

sealed class MenuItem(
    val title: String,
    val icon: ImageVector? = null,
    val route: String? = null
) {
    data class Parent(
        val parentTitle: String,
        val parentIcon: ImageVector?,
        val children: List<Child>,
        var isExpanded: Boolean = false
    ) : MenuItem(title = parentTitle, icon = parentIcon)

    data class Child(
        val childTitle: String,
        val childIcon: ImageVector? = null,
        val childRoute: String
    ) : MenuItem(title = childTitle, icon = childIcon, route = childRoute)

    data object Divider : MenuItem(title = "divider_item")

    data class ActionItem(
        val actionTitle: String,
        val actionIcon: ImageVector?,
        val onClickAction: () -> Unit
    ) : MenuItem(title = actionTitle, icon = actionIcon)
}

fun getMenuItems(onAboutClick: () -> Unit): List<MenuItem> {
    return listOf(
        MenuItem.Parent(
            parentTitle = "Конфигурация",
            parentIcon = Icons.Filled.Build,
            children = listOf(
                MenuItem.Child(
                    childTitle = "Устройства",
                    childRoute = NavRoutes.MENU_APP_CONFIG_DEV
                ),
                MenuItem.Child(
                    childTitle = "Схемы доступа",
                    childRoute = NavRoutes.MENU_APP_CONFIG_SHEM
                )
            )
        ),
        MenuItem.Parent(
            parentTitle = "Бюро Пропусков",
            parentIcon = Icons.Filled.AccountBox,
            children = listOf(
                MenuItem.Child(
                    childTitle = "Пользователи",
                    childRoute = NavRoutes.MENU_APP_BUREAU_USR
                ),
                MenuItem.Child(
                    childTitle = "Ключи",
                    childRoute = NavRoutes.MENU_APP_BUREAU_KEYS
                )
            )
        ),
        MenuItem.Parent(
            parentTitle = "События",
            parentIcon = Icons.Filled.Notifications,
            children = listOf(
                MenuItem.Child(
                    childTitle = "Журнал событий",
                    childRoute = NavRoutes.MENU_APP_EVENTS_EVENT_LOG
                )
            )
        ),
        MenuItem.Divider,
        MenuItem.ActionItem(
            actionTitle = "О программе",
            actionIcon = Icons.Filled.Info,
            onClickAction = onAboutClick
        ),
        MenuItem.ActionItem(
            actionTitle = "Выход",
            actionIcon = Icons.AutoMirrored.Filled.ExitToApp,
            onClickAction = { exitApp() }
        )
    )
}