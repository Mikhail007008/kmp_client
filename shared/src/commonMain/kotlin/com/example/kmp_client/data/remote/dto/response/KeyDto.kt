package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class KeyDto(
    val id: String,
    val expiration: String? = null,
    val blocked: Boolean,
    val user: Long,
    val fullName: String? = null,
    val guestKey: Boolean,
    val systemKeyMode: Int,
    val reentryAlwaysAllowed: Boolean,
    val extCode: Int,
    val description: String? = null
)