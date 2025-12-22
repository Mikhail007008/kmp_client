package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val accessTokenExpiry: String,
    val refreshToken: String,
    val refreshTokenExpiry: String
)