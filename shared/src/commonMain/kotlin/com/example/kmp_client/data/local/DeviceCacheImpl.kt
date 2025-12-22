/**
 * Реализация кеша для устройств скуд
 * Сохраняет и загружает данные устройств в локальном хранилище с поддержкой Flow
 */
package com.example.kmp_client.data.local

import com.example.kmp_client.domain.model.device.Device
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSettingsApi::class)
class DeviceCacheImpl(
    private val settings: ObservableSettings,
    private val json: Json
): DeviceCache {
    private val devicesKey = "cached_devices_list"

    override suspend fun saveDevices(devices: List<Device>) {
        try {
            val devicesJson = json.encodeToString(devices)
            settings.putString(devicesKey, devicesJson)
        } catch (_: Exception) {
            println("Ошибка сохранения устройств")
        }
    }

    private fun parseDevicesJson(devicesJson: String?): List<Device> {
        return if (devicesJson.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<Device>>(devicesJson)
            } catch (_: Exception) {
                println("Ошибка парсинга устройств JSON")
                emptyList()
            }
        }
    }

    override fun getDevicesMapFlow(): Flow<Map<String, String>> {
        return settings.getStringFlow(devicesKey, "")
            .map { devicesJson ->
                parseDevicesJson(devicesJson).associate { it.id to it.name }
            }
    }

    override suspend fun getDevicesMap(): Map<String, String> {
        val devicesJson = settings.getString(devicesKey, "")
        return parseDevicesJson(devicesJson).associate { it.id to it.name }
    }

    override suspend fun clearDevices() {
        settings.remove(devicesKey)
    }
}