/**
 * Реализация репозитория для работы с событиями скуд
 * Обрабатывает API запросы для получения событий с кешированием устройств
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.local.DeviceCache
import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService

class EventsRepositoryImpl(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val deviceCache: DeviceCache,
) : EventsRepository {
}