package com.example.kmp_client.data.remote.dto.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class EventPostRequest(
    val skip: Int,
    val limit: Int,
    val orderBy: String? = null,
    val addTotal: Int = 1,
    val fullName: Boolean = true,
    val filters: PostFiltersBody
)

@Serializable
data class PostFiltersBody(
    val `$and`: List<Map<String, JsonElement>>
)