package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class KeyDetailResponseDto(
    val id: String,
    val expiration: String? = null,
    val blocked: Boolean,
    val user: Long,
    val guestKey: Boolean,
    val guestKeyAccessSchemes: List<Long> = emptyList(),
    val systemKeyMode: Int,
    val reentryAlwaysAllowed: Boolean,
    val extCode: Int,
    val description: String
)
