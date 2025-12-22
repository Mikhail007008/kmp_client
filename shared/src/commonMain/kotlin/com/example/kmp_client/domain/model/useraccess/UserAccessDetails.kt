package com.example.kmp_client.domain.model.useraccess

data class UserAccessSchemeDetail(
    val id: Long,
    val name: String,
    val anytime: Boolean,
    val currentYearScheduleId: Long?,
    val currentYearScheduleName: String?,
    val nextYearScheduleId: Long?,
    val nextYearScheduleName: String?
)

data class UserAccessKeyDetail(
    val key: String,
    val controlKey: String?,
    val accessSchemes: List<UserAccessSchemeDetail>
)

data class UserAccessDetails(
    val keys: List<UserAccessKeyDetail>
)