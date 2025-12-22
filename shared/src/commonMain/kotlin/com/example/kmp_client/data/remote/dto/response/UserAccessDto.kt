package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccessInfoResponse(
    @SerialName("access") val accessEntries: List<AccessEntryDto>
)

@Serializable
data class AccessEntryDto(
    val key: String,
    val controlKey: String?,
    val accessSchemes: List<AccessSchemeInUserAccessDto>
)

@Serializable
data class AccessSchemeInUserAccessDto(
    val id: Long,
    val name: String,
    val anytime: Boolean,
    val scheduleCurrentYear: Long?,
    val scheduleNextYear: Long?
)