/**
 * Интерфейс репозитория для работы со схемами доступа
 * Определяет методы получения списка схем доступа
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.domain.model.accesssheme.AccessScheme

interface AccessSchemesRepository {
suspend fun getAccessSchemes(): Result<List<AccessScheme>>
}