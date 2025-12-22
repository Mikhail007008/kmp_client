package com.example.kmp_client.presentation.screen.users

data class AddUserState(
    val value: String = "",
    val errorMessage: String? = null,
    val selectedId: Long? = null
)
