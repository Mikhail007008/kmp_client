/**
 * Реализация репозитория для аутентификации пользователей
 * Обрабатывает API запросы для входа в систему и управления сессиями
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

}