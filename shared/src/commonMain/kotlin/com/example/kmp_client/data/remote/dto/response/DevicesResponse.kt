package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DevicesResponse(
    val devices: List<DeviceDTO>
)

@Serializable
data class DeviceDTO(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int,
    val mod: Int,
    val status: Int,
    val synchronized: Boolean
)