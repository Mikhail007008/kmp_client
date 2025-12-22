/**
 * Реализация репозитория для работы с устройствами скуд
 * Обрабатывает API запросы для получения списка устройств
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService

class DevicesRepositoryImpl(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage
    ): DevicesRepository {
}