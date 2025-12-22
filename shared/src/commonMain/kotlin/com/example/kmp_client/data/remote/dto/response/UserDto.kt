package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val code: String?,
    val name: String?,
    val surname: String,
    val patronymic: String?,
    val jobTitleId: Long?,
    val departmentId: Long?,
    val guest: Boolean?,
    val photo: Boolean?,
    val phone: String?,
    val email: String?,
    val balance: Double?,
    val creationTime: String?
)
