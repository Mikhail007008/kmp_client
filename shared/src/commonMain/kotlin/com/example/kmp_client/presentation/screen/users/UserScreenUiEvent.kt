package com.example.kmp_client.presentation.screen.users

import androidx.compose.material3.SnackbarDuration

sealed class UserScreenUiEvent {
    data class ShowSnackbar(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short,
    ) : UserScreenUiEvent()

    data class ShowErrorDialog(val title: String, val errors: List<String>) :
        UserScreenUiEvent()
}