package com.example.kmp_client.domain.model.user

import kotlinx.datetime.Clock.System

data class UserDeletionEvent(
    val success: Boolean,
    val timestamp: Long = System.now().toEpochMilliseconds(),
)

data class UserUpdateEvent(
    val success: Boolean,
    val userId: Long,
    val timestamp: Long = System.now().toEpochMilliseconds(),
)