/**
 * Интерфейс репозитория для аутентификации пользователей
 * Управляет входом в систему, выходом и сессиями пользователей
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.remote.dto.request.AuthRequest
import com.example.kmp_client.domain.model.auth.SessionData

interface AuthRepository {
    suspend fun login(
        serverUrl: String,
        port: String,
        authRequest: AuthRequest
    ): Result<SessionData>

    suspend fun logout(): Result<Unit>
}