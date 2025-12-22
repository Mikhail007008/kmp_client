package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeysListResponse(
    @SerialName("keys")
    val keys: List<KeyDto>,
    val total: Int? = null,
    val skip: Int,
    val limit: Int,
    val count: Int
)
