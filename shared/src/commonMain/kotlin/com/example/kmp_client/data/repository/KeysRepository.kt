/**
 * Интерфейс репозитория для работы с ключами доступа системы
 * Определяет методы получения, создания, удаления ключей и получения детальной информации
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.remote.dto.request.KeyCreationRequest
import com.example.kmp_client.domain.model.key.KeyDetails
import com.example.kmp_client.domain.model.key.PaginatedKeys

interface KeysRepository {
    suspend fun getKeys(
        skip: Int,
        limit: Int,
        filters: Map<String, String>? = null,
        includeFullName: Boolean = true
    ): Result<PaginatedKeys>

    suspend fun getKeyDetails(keyId: String): Result<KeyDetails>
    suspend fun createKey(keyData: KeyCreationRequest): Result<Unit>
    suspend fun deleteKey(keyId: String): Result<Unit>
}