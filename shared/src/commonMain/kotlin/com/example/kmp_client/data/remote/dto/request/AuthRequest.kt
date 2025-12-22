package com.example.kmp_client.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val user: String,
    val password: String
)