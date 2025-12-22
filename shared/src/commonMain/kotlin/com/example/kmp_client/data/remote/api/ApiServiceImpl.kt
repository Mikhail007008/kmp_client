/**
 * Реализация API сервиса для выполнения HTTP запросов
 * Управляет аутентификацией, токенами и сериализацией данных
 */
package com.example.kmp_client.data.remote.api

import com.example.kmp_client.data.local.storage.TokenStorage
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

class ApiServiceImpl(
    private val authHttpClient: HttpClient,
    private val apiHttpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val customGetRequest: CustomGetRequest,
    private val json: Json
) : ApiService {
}