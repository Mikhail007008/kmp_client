/**
 * Реализация репозитория для работы со схемами доступа
 * Обрабатывает API запросы для получения списка схем доступа
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService

class AccessSchemesRepositoryImpl(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage
) : AccessSchemesRepository {
}