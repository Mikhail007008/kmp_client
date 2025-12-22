/**
 * Реализация репозитория для работы с ключами доступа системы
 * Обрабатывает API запросы для управления ключами с поддержкой пользователей и схем доступа
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService
import kotlinx.serialization.json.Json

class KeysRepositoryImpl(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val usersRepository: UsersRepository,
    private val accessSchemesRepository: AccessSchemesRepository,
    private val json: Json
) : KeysRepository {
}