/**
 * Основной компонент навигации приложения
 * Управляет переходами между экранами авторизации и основного приложения с анимацией
 */
package com.example.kmp_client.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kmp_client.presentation.screen.auth.AuthScreen
import com.example.kmp_client.presentation.screen.auth.AuthUIState
import com.example.kmp_client.presentation.screen.auth.AuthViewModel
import com.example.kmp_client.presentation.screen.main.MainAppScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.transition.NavTransition

@Composable
fun AppNavigation(
    navigator: Navigator,
    authViewModel: AuthViewModel,
    authUiState: AuthUIState
) {
    val initialRoute = if (authUiState.isAuthenticated) NavRoutes.MENU_APP_CONFIG_DEV else NavRoutes.LOGIN
    val currentBackStackEntry by navigator.currentEntry.collectAsState(null)

    LaunchedEffect(authUiState.isAuthenticated) {
        val currentRoute = currentBackStackEntry?.route?.route

        if (!authUiState.isLoading && currentRoute != null) {
            val targetRoute = if (authUiState.isAuthenticated) NavRoutes.MENU_APP_CONFIG_DEV else NavRoutes.LOGIN

            if (currentRoute != targetRoute) {
                navigator.navigate(
                    targetRoute,
                    NavOptions(
                        popUpTo = PopUpTo.First(inclusive = true),
                        launchSingleTop = true
                    )
                )
            }
        } else if (!authUiState.isLoading && currentRoute == null && initialRoute != (if (authUiState.isAuthenticated) NavRoutes.MENU_APP_CONFIG_DEV else NavRoutes.LOGIN)) {
            val targetRoute = if (authUiState.isAuthenticated) NavRoutes.MENU_APP_CONFIG_DEV else NavRoutes.LOGIN
            navigator.navigate(
                targetRoute,
                NavOptions(
                    popUpTo = PopUpTo.First(inclusive = true),
                    launchSingleTop = true
                )
            )
        }
    }

    NavHost(
        navigator = navigator,
        initialRoute = initialRoute,
        navTransition = NavTransition(
            createTransition = fadeIn(animationSpec = tween(250)),
            destroyTransition = fadeOut(animationSpec = tween(250)),
            pauseTransition = fadeOut(animationSpec = tween(250)),
            resumeTransition = fadeIn(animationSpec = tween(250))
        )
    ) {
        scene(NavRoutes.LOGIN) {
            AuthScreen(
                viewModel = authViewModel
            )
        }

        scene(NavRoutes.MENU_APP_CONFIG_DEV) {
            MainAppScreen(
                appNavigator = navigator,
                authViewModel = authViewModel
            )
        }
    }
}