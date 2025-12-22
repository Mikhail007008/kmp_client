/**
 * Интерфейс репозитория для работы с событиями скуд
 * Определяет методы получения событий с фильтрацией и пагинацией
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.remote.dto.request.EventPostRequest
import com.example.kmp_client.domain.model.event.PaginatedEvents

interface EventsRepository {
    suspend fun getEvents(
        skip: Int,
        limit: Int,
        filters: Map<String, String>? = null
    ): Result<PaginatedEvents>

    suspend fun getEventsPost(requestBody: EventPostRequest): Result<PaginatedEvents>
}