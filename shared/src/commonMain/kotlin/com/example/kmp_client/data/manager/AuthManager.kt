/**
 * Менеджер аутентификации для управления токенами и сессиями
 * Реализует TokenProvider, обеспечивает валидацию и обновление токенов
 */
package com.example.kmp_client.data.manager

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.ApiService
import com.example.kmp_client.domain.provider.TokenProvider

class AuthManager(
    private val tokenStorage: TokenStorage,
    private val apiService: ApiService,
) : TokenProvider {

}