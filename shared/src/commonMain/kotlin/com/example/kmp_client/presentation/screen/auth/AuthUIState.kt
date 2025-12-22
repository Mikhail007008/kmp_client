package com.example.kmp_client.presentation.screen.auth

data class AuthUIState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val serverUrl: String = "",
    val port: String = "",
    val username: String = "",
    val password: String = "",
    val autoAuthEnabled: Boolean = false,
    val countdown: Int = 0,
    val isAutoAuthMode: Boolean = false,
    val passwordVisible: Boolean = false
)