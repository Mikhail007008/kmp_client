package com.example.kmp_client.domain.model.key

import kotlinx.datetime.Instant

data class KeyDetails(
    val id: String,
    val uid: String,
    val keyType: KeyType,
    val assignedUser: AssignedUser? = null,
    val expirationDate: Instant? = null,
    val isBlocked: Boolean,
    val comment: String,
    val reentryAlwaysAllowed: Boolean?,
    val extCodeRegular: Int?,
    val guestAccessSchemes: List<AccessSchemeSelection>? = null,
    val extCodeGuest: Int?,
    val systemKeyMode: SystemKeyModeAction?
)

data class AssignedUser(
    val id: Long,
    val fullName: String,
    val surname: String,
    val name: String,
    val patronymic: String
)
