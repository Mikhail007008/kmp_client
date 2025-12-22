package com.example.kmp_client.domain.model.user

data class PaginatedUsers(
    val users: List<User>,
    val totalCount: Int,
    val canLoadMore: Boolean,
    val discoveredFeatureKeys: List<String> = emptyList()
)
