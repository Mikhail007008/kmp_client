package com.example.kmp_client.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class Department(
    val id: Long,
    val name: String,
    val abbreviation: String? = null,
    val parent: Long?,
    val children: List<Department> = emptyList()
)

@Serializable
data class DepartmentsResponse(
    val departments: List<Department>
)
