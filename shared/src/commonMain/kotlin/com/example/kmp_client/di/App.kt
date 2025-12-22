/**
 * Корневой Composable компонент KMP приложения
 * Настраивает тему (светлая/темная), навигацию и DI через Koin
 */
package com.example.kmp_client.di

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kmp_client.presentation.navigation.AppNavigation
import com.example.kmp_client.presentation.screen.auth.AuthViewModel
import com.example.kmp_client.presentation.ui.theme.CustomTypography
import com.example.kmp_client.presentation.ui.theme.DarkColors
import com.example.kmp_client.presentation.ui.theme.LightColors
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import org.koin.compose.koinInject

@Composable
fun App() {
    val useDarkTheme = isSystemInDarkTheme()
    val colors = if (useDarkTheme) DarkColors else LightColors
    val typography = CustomTypography()
    val shapes = MaterialTheme.shapes

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes
    ) {
        PreComposeApp {
            val navigator = rememberNavigator()
            val authViewModel: AuthViewModel = koinInject()
            val authUiState by authViewModel.uiState.collectAsState()

            AppNavigation(
                navigator = navigator,
                authViewModel = authViewModel,
                authUiState = authUiState
            )
        }
    }
}