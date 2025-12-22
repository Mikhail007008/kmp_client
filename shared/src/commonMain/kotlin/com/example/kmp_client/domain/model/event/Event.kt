/**
 * Доменная модель событий в скуд
 * Содержит информацию о времени, устройстве, пользователе и ключе для каждого события
 */
package com.example.kmp_client.domain.model.event

import kotlinx.datetime.LocalDateTime

data class Event(
    val id: Long,
    val originalDateTime: LocalDateTime,
    val formattedDateTime: String,
    val deviceName: String,
    val eventDescription: String,
    val readerDescription: String,
    val userFullName: String,
    val keyNumber: String
)
