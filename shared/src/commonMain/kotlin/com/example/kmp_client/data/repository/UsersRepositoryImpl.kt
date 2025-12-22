/**
 * Реализация репозитория для работы с пользователями скуд
 * Обрабатывает API запросы для управления пользователями с сериализацией JSON
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService
import kotlinx.serialization.json.Json

class UsersRepositoryImpl(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val json: Json
) : UsersRepository {
}