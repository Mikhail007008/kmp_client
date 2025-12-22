/**
 * Интерфейс кеша для устройств скуд
 * Определяет методы сохранения и загрузки данных устройств с поддержкой Flow
 */
package com.example.kmp_client.data.local

import com.example.kmp_client.domain.model.device.Device
import kotlinx.coroutines.flow.Flow

interface DeviceCache {
    suspend fun saveDevices(devices: List<Device>)
    fun getDevicesMapFlow(): Flow<Map<String, String>>
    suspend fun getDevicesMap(): Map<String, String>
    suspend fun clearDevices()
}