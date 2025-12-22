package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SchedulesResponse(
    val schedules: List<ScheduleDto>
)

@Serializable
data class ScheduleDto(
    val id: Long,
    val name: String,
    val year: Int,
    val defaultForNewEmployees: Boolean,
    val defaultForNewGuests: Boolean
)