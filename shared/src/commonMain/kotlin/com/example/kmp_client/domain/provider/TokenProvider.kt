/**
 * Интерфейс провайдера токенов для управления аутентификацией
 * Определяет методы получения, обновления и очистки токенов доступа
 */
package com.example.kmp_client.domain.provider

interface TokenProvider {
    suspend fun getValidAccessToken(): String?
    suspend fun forceRefreshToken(): String?
    suspend fun clearSession()
}