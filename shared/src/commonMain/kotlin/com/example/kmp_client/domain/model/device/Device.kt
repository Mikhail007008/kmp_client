/**
 * Доменные модели устройств системы контроля доступа
 * Включает информацию о типе устройства, режиме работы и состоянии синхронизации
 */
package com.example.kmp_client.domain.model.device

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val modelType: DeviceModelType,
    val operatingMode: DeviceOperatingMode,
    val isSynchronized: Boolean
)

@Serializable
enum class DeviceModelType(val modValue: Int, val displayName: String) {
    ERA_500(4, "ЭРА-500"),
    ERA_2000(5, "ЭРА-2000"),
    ERA_2000_M(6, "ЭРА-2000М"),
    ERA_10000_V2(8, "ЭРА-10000 v2"),
    ERA_2000_INFORCER(9, "ЭРА-2000 Инфорсер"),
    ERA_2000_V2(8, "ЭРА-2000 v2"),
    ERA_2000_GSM(7, "ЭРА-2000 v2 GSM"),
    ERA_10000_V2_M(11, "ЭРА-10000М v2"),
    ERA_2000_V2_M(11, "ЭРА-2000М v2"),
    ERA_10000_GSM(7, "ЭРА-10000 v2 GSM"),
    ERA_60000_V2(14, "ЭРА-60000 v2"),
    UNKNOWN(-1, "нет информации");

    companion object {
        fun fromMod(mod: Int): DeviceModelType {
            return entries.find { it.modValue == mod } ?: UNKNOWN
        }

        fun fromModAndMac(mod: Int, macAddress: String): DeviceModelType {
            return when {
                mod == 11 && macAddress.startsWith("000B3BF0") -> ERA_10000_V2_M
                mod == 11 && macAddress.startsWith("000B3B00") -> ERA_2000_V2_M
                mod == 8 && macAddress.startsWith("000B3BF0") -> ERA_10000_V2
                mod == 8 && macAddress.startsWith("000B3B00") -> ERA_2000_V2
                mod == 7 && macAddress.startsWith("000B3BF0") -> ERA_10000_GSM
                mod == 7 && macAddress.startsWith("000B3B00") -> ERA_2000_GSM
                else -> fromMod(mod)
            }
        }
    }
}

@Serializable
enum class DeviceOperatingMode(val statusValue: Int, val displayName: String) {
    CONTROL(*, "контроль"),
    OPEN(*, "открыт"),
    CLOSED(*, "закрыт"),
    DISABLED(*, "отключен"),
    UNKNOWN(*, "Н");

    companion object {
        fun fromStatus(status: Int): DeviceOperatingMode {
            return entries.find { it.statusValue == status } ?: UNKNOWN
        }
    }
}