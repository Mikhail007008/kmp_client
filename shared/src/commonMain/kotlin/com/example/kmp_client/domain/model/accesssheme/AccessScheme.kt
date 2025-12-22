/**
 * Доменная модель схемы доступа системы
 * Определяет доступные устройства и настройки по умолчанию для сотрудников и гостей
 */
package com.example.kmp_client.domain.model.accesssheme

data class AccessScheme(
    val id: Long,
    val name: String,
    val devices: List<String>,
    val defaultForNewEmployees: Boolean,
    val defaultForNewGuests: Boolean
)