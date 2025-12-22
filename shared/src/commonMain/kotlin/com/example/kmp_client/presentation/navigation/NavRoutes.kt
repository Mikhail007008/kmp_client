package com.example.kmp_client.presentation.navigation

object NavRoutes {
    const val LOGIN = "/login"
    const val MENU_APP_CONFIG_DEV = "/devices"
    const val MENU_APP_CONFIG_SHEM = "config/shem"
    const val MENU_APP_BUREAU_USR = "bureau/usr"
    const val MENU_APP_BUREAU_KEYS = "bureau/keys"
    const val MENU_APP_EVENTS_EVENT_LOG = "events/event_log"
    const val USER_DETAIL_ROTE = "user-detail"
    const val USER_ID_ARG = "userId"
    const val USER_DETAIL = "$USER_DETAIL_ROTE/{$USER_ID_ARG}"

    fun userDetailScreen(userId: Long): String {
        return "$USER_DETAIL_ROTE/$userId"
    }

    private val mainScreenRoutes = setOf(
        MENU_APP_CONFIG_DEV,
        MENU_APP_CONFIG_SHEM,
        MENU_APP_BUREAU_USR,
        MENU_APP_BUREAU_KEYS,
        MENU_APP_EVENTS_EVENT_LOG
    )

    fun isMainScreenRoute(route: String?): Boolean {
        return route != null && mainScreenRoutes.contains(route.split("/").firstOrNull())
    }
}