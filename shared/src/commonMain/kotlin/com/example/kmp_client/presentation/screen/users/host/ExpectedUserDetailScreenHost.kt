package com.example.kmp_client.presentation.screen.users.host

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.Navigator

@Composable
expect fun ExpectedUserDetailScreenHost(
    userId: Long,
    navController: Navigator,
    snackbarHostState: SnackbarHostState
)