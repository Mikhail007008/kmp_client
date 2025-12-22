package com.example.kmp_client.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserAccessRequest(
    val reassignKeys: Boolean = false,
    val access: List<UserAccessKeyPayload>
)

@Serializable
data class UserAccessKeyPayload(
    val key: String,
    val controlKey: String = "",
    val accessSchemes: List<UserAccessSchemePayload>
)

@Serializable
data class UserAccessSchemePayload(
    val id: Long,
    val name: String,
    val anytime: Boolean,
    val scheduleCurrentYear: Long,
    val scheduleNextYear: Long
)