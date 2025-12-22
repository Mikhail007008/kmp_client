package com.example.kmp_client.presentation.screen.devices

import com.example.kmp_client.domain.model.device.Device

data class DevicesUIState(
    val isLoading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val error: String? = null
)
