package com.example.kmp_client.data.remote.api

import com.example.kmp_client.data.remote.dto.response.EventsListResponse
import kotlinx.serialization.KSerializer
/**
 * Класс для отправки GET запроса с телом, обходящий ограничения Ktor
 */
expect class CustomGetRequest {
    suspend fun <T> sendGetRequestWithBody(
        serverUrl: String,
        port: String,
        path: String,
        body: T,
        serializer: KSerializer<T>,
        token: String? = null,
    ): EventsListResponse
}