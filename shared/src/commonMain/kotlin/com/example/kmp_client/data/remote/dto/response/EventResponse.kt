package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class EventsListResponse(
    val events: List<EventsDto>,
    val skip: Int,
    val limit: Int,
    val count: Int,
    val total: Int? = null
)

@Serializable
data class EventsDto(
    val id: Long,
    val dateTime: String,
    val device: String,
    val code1: Int,
    val code2: Int,
    val user: Long,
    val key: String,
    val fullName: String? = null
)