package com.example.kmp_client.data.remote.api

import com.example.kmp_client.core.network.NetworkException
import com.example.kmp_client.data.remote.dto.response.EventsListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.KSerializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Android реализация для отправки GET запросов с телом
 * Использует OkHttp с POST + X-HTTP-Method-Override для корректной отправки тела
 */
actual class CustomGetRequest(
    private val json: Json,
) {

    actual suspend fun <T> sendGetRequestWithBody(
        serverUrl: String,
        port: String,
        path: String,
        body: T,
        serializer: KSerializer<T>,
        token: String?,
    ): EventsListResponse = withContext(Dispatchers.IO) {
        try {
            val jsonBody = json.encodeToString(serializer, body)
            val urlString = "http://$serverUrl:$port$path"
            val client = OkHttpClient()
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
            val requestBuilder = Request.Builder()
                .url(urlString)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "KMP-EraClient/1.0")
                .addHeader("X-HTTP-Method-Override", "GET")

            if (token != null && token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val responseHeaders = response.headers
            for (i in 0 until responseHeaders.size) {
                println("-> ${responseHeaders.name(i)}: ${responseHeaders.value(i)}")
            }

            if (responseBody.isEmpty() && !response.isSuccessful) {
                throw NetworkException.ConnectionException(
                    "Получен пустой ответ от сервера (код: ${response.code})"
                )
            }

            return@withContext try {
                json.decodeFromString(EventsListResponse.serializer(), responseBody)
            } catch (e: Exception) {
                throw NetworkException.ParsingException(
                    message = "Ошибка обработки ответа сервера. Ответ: $responseBody",
                    cause = e
                )
            }

        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            throw NetworkException.ConnectionException(
                "Ошибка при выполнении GET запроса с телом: ${e.message}"
            )
        }
    }
}