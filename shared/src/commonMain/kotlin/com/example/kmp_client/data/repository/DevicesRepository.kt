/**
 * Интерфейс репозитория для работы с устройствами скуд
 * Определяет методы получения списка устройств
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.domain.model.device.Device

interface DevicesRepository {
    suspend fun fetchDevices(): Result<List<Device>>
}