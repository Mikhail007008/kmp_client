package com.example.kmp_client.data.remote.dto.mapper

import com.example.kmp_client.data.remote.dto.response.KeyDetailResponseDto
import com.example.kmp_client.data.remote.dto.response.KeyDto
import com.example.kmp_client.domain.model.key.AccessSchemeSelection
import com.example.kmp_client.domain.model.key.AssignedUser
import com.example.kmp_client.domain.model.key.Key
import com.example.kmp_client.domain.model.key.KeyDetails
import com.example.kmp_client.domain.model.key.KeyType
import com.example.kmp_client.domain.model.key.SystemKeyModeAction
import com.example.kmp_client.domain.util.DateTimeFormatters
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun KeyDto.toDomain(): Key {
    val deviceTimeZone = TimeZone.currentSystemDefault()

    val parsedExpirationInstant: Instant? = try {
        this.expiration?.takeIf { it.isNotBlank() }?.let{ serverDateTime ->
            val localDateTime = LocalDateTime.parse(serverDateTime)
            localDateTime.toInstant(deviceTimeZone)
        }
    } catch (_: Exception) {
        null
    }

    val expirationDisplayValue = parsedExpirationInstant?.let {utcInstant ->
        try {
            val localExpirationDateTime = utcInstant.toLocalDateTime(deviceTimeZone)
            "${localExpirationDateTime.format(DateTimeFormatters.displayDateTimeFormatter)} (включительно)"
        } catch (_: Exception) {
            "Ошибка даты"
        }
    } ?: "Бессрочный"

    val isExpiredNow = parsedExpirationInstant?.let { expInstant ->
        expInstant < Clock.System.now()
    } ?: false

    val statusDisplayValue = when {
        this.blocked -> "Заблокирован"
        isExpiredNow -> "Просрочен"
        else -> "Активен"
    }

    return Key(
        id = this.id,
        expirationInstant = parsedExpirationInstant,
        expirationDisplay = expirationDisplayValue,
        isBlocked = this.blocked,
        userFullName = this.fullName?.takeIf { it.isNotBlank() } ?: "Не указано",
        isGuestKey = this.guestKey,
        isSystemKey = this.systemKeyMode != 0,
        systemKeyModeValue = this.systemKeyMode,
        alwaysReentryAllowed = this.reentryAlwaysAllowed,
        alwaysReentryAllowedDisplay = if (this.reentryAlwaysAllowed) "Да" else "Нет",
        comment = this.description?.takeIf { it.isNotBlank() },
        isExpired = isExpiredNow,
        statusDisplay = statusDisplayValue
    )
}

private fun setKeyTypeFromDetailDto(dto: KeyDetailResponseDto): KeyType {
    if (dto.guestKey) {
        return KeyType.GUEST
    }
    if (dto.systemKeyMode > 0) {
        return KeyType.SYSTEM
    }

    return KeyType.REGULAR
}

fun KeyDetailResponseDto.toDomain(
    assignedUser: AssignedUser?,
    allAvailableAccessSchemes: Map<String, String>
): KeyDetails {
    val keyType = setKeyTypeFromDetailDto(this)
    val expirationInstant = this.expiration?.let {
        try {
            Instant.parse(it)
        } catch (_: Exception) {
            null
        }
    }

    return KeyDetails(
        id = this.id,
        uid = this.id,
        keyType = keyType,
        assignedUser = assignedUser,
        expirationDate = if (keyType == KeyType.REGULAR) expirationInstant else null,
        isBlocked = this.blocked,
        comment = this.description,
        reentryAlwaysAllowed = if (keyType == KeyType.REGULAR) this.reentryAlwaysAllowed else null,
        extCodeRegular = if (keyType == KeyType.REGULAR) this.extCode else 0,
        guestAccessSchemes = if (keyType == KeyType.GUEST) {
            val selectedSchemesIds = this.guestKeyAccessSchemes.toSet()
            allAvailableAccessSchemes.map { (id, name) ->
                AccessSchemeSelection(id, name, selectedSchemesIds.contains(id.toLongOrNull()))
            }.sortedBy { it.name }
        } else null,
        extCodeGuest = if (keyType == KeyType.GUEST) this.extCode else 0,
        systemKeyMode = if (keyType == KeyType.SYSTEM) SystemKeyModeAction.fromApiValue(this.systemKeyMode) else null
    )
}

fun formatExpirationForDisplay(expirationInstant: Instant?): String {
    return expirationInstant?.let {instant ->
        try {
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val localDate = localDateTime.date
            localDate.format(DateTimeFormatters.ddMMyyFormatter)
        } catch (_: Exception) {
            "Ошибка даты"
        }
    } ?: ""
}