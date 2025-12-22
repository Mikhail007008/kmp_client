package com.example.kmp_client.data.remote.dto.mapper

import com.example.kmp_client.data.remote.dto.response.UserDto
import com.example.kmp_client.domain.model.user.User
import kotlinx.datetime.LocalDateTime

fun UserDto.toDomain(featuresMap: Map<String, String?> = emptyMap()): User {
    return User(
        id = this.id,
        code = this.code ?: "",
        name = this.name ?: "",
        surname = this.surname,
        patronymic = this.patronymic ?: "",
        jobTitleId = this.jobTitleId,
        departmentId = this.departmentId,
        guest = this.guest == true,
        photo = this.photo == true,
        phone = this.phone ?: "",
        email = this.email ?: "",
        balance = this.balance ?: 0.0,
        creationTime = try {
            this.creationTime?.takeIf { it.isNotBlank() }?.let { dateTimeString ->
                val localDateTime = LocalDateTime.parse(dateTimeString)
                localDateTime.date
            }
        } catch (_: Exception) {
            null
        },
        features = featuresMap.mapValues { it.value ?: "" }
    )
}