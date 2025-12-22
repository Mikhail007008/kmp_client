package com.example.kmp_client.domain.model.event

data class PaginatedEvents(
    val items: List<Event>,
    val totalCount: Int,
    val canLoadMore: Boolean
)
