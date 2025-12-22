/**
 * Доменные модели ключей доступа системы
 * Содержат информацию о сроках действия, статусе, владельце и настройках ключей
 */
package com.example.kmp_client.domain.model.key

import kotlinx.datetime.Instant

data class Key(
    val id: String,
    val expirationInstant: Instant?,
    val expirationDisplay: String,
    val isBlocked: Boolean,
    val statusDisplay: String,
    val userFullName: String?,
    val isGuestKey: Boolean,
    val isSystemKey: Boolean,
    val systemKeyModeValue: Int,
    val alwaysReentryAllowed: Boolean,
    val alwaysReentryAllowedDisplay: String,
    val comment: String?,
    val isExpired: Boolean
)

data class PaginatedKeys(
    val items: List<Key>,
    val totalCount: Int,
    val canLoadMore: Boolean
)