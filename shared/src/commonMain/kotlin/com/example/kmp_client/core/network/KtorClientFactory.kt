/**
 * Фабрика для создания HTTP клиентов Ktor
 * Конфигурирует клиенты для аутентификации и API запросов с поддержкой токенов
 */
package com.example.kmp_client.core.network

import com.example.kmp_client.domain.provider.TokenProvider
import io.ktor.client.HttpClient
import kotlinx.serialization.ExperimentalSerializationApi

object KtorClientFactory {
    @OptIn(ExperimentalSerializationApi::class)
    fun createApiHttpClient(
        tokenProviderFactory: () -> TokenProvider,
    ): HttpClient {

    }
}