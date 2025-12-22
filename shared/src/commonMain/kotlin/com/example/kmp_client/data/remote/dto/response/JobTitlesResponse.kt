package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JobTitle(
    val id: Long,
    val name: String
)

@Serializable
data class JobTitlesResponse(
    val jobTitles: List<JobTitle>
)
