package com.example.kmp_client.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val code: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val jobTitleId: Long? = null,
    val departmentId: Long? = null,
    val features: Map<String, String?>? = null,
    val phone: String? = null,
    val email: String? = null,
    val balance: Double? = null
)
