package com.example.kmp_client.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class KeyCreationWrapper(
    val keys: List<KeyCreationRequest>
)

@Serializable
data class KeyCreationRequest(
    val id: String,
    val expiration: String? = null,
    val reentryAlwaysAllowed: Boolean? = null,
    val extCode: Int? = null,
    val description: String? = null
)
