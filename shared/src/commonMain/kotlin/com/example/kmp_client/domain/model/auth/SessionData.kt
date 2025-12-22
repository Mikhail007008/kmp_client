package com.example.kmp_client.domain.model.auth

import kotlinx.datetime.Instant

data class SessionData(
    val accessToken: String,
    val accessTokenExpiry: Instant,
    val refreshToken: String,
    val refreshTokenExpiry: Instant
)
