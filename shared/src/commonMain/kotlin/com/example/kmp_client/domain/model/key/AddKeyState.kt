package com.example.kmp_client.domain.model.key

data class KeyInputFieldState(
    val value: String = "",
    val errorMessage: String? = null
)

data class ReentryAllowedState(
    val value: Boolean? = null,
    val errorMessage: String? = null
)

data class ExtCodeState(
    val selectionOptions: Set<Int> = emptySet(),
    val calculatedValue: Int = 0,
    val errorMessage: String? = null
)

data class AddKeyDialogState(
    val uid: KeyInputFieldState = KeyInputFieldState(),
    val expiration: KeyInputFieldState = KeyInputFieldState(),
    val reentryAlwaysAllowed: ReentryAllowedState = ReentryAllowedState(),
    val extCode: ExtCodeState = ExtCodeState(),
    val description: KeyInputFieldState = KeyInputFieldState(),
    val overallErrorMessage: String? = null
)