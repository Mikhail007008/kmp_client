package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessSchemesResponse(
    @SerialName("access-schemes")
    val accessSchemes: List<AccessSchemeDTO>
)

@Serializable
data class AccessSchemeDTO(
    val id: Long,
    val name: String,
    val defaultForNewEmployees: Boolean,
    val defaultForNewGuests: Boolean,
    val devices: List<String>
)
