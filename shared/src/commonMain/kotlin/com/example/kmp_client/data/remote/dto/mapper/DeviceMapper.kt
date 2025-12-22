package com.example.kmp_client.data.remote.dto.mapper

import com.example.kmp_client.data.remote.dto.response.DeviceDTO
import com.example.kmp_client.domain.model.device.Device
import com.example.kmp_client.domain.model.device.DeviceModelType
import com.example.kmp_client.domain.model.device.DeviceOperatingMode

fun DeviceDTO.toDomain(): Device {
    return Device(
        id = this.id,
        name = this.name,
        ipAddress = this.ip,
        port = this.port,
        modelType = DeviceModelType.fromModAndMac(this.mod, this.id),
        operatingMode = DeviceOperatingMode.fromStatus(this.status),
        isSynchronized = this.synchronized
    )
}

fun List<DeviceDTO>.toDomain(): List<Device> {
    return this.map { it.toDomain() }
}