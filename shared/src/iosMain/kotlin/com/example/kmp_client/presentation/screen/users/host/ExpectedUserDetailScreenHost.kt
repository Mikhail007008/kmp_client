package com.example.kmp_client.presentation.screen.users.host

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.example.kmp_client.presentation.screen.users.SharedAppViewModel
import com.example.kmp_client.presentation.screen.users.UserDetailScreen
import com.example.kmp_client.presentation.screen.users.UserDetailViewModel
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.navigation.Navigator
import org.koin.core.parameter.parametersOf

@Composable
actual fun ExpectedUserDetailScreenHost(
    userId: Long,
    navController: Navigator,
    snackbarHostState: SnackbarHostState
) {
    val sharedViewModel: SharedAppViewModel = koinViewModel(SharedAppViewModel::class)
    val userDetailViewModel: UserDetailViewModel = koinViewModel(
        UserDetailViewModel::class,
        key = userId.toString()
    ) { parametersOf(userId, sharedViewModel) }

    UserDetailScreen(
        userId = userId,
        viewModel = userDetailViewModel,
        sharedViewModel = sharedViewModel,
        navController = navController,
        snackbarHostState = snackbarHostState
    )
}