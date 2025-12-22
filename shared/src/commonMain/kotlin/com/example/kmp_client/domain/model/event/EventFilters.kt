package com.example.kmp_client.domain.model.event

data class EventFilters(
    val selectedDeviceIds: Set<String> = emptySet(),
    val selectedEventType: EventTypeFilter = EventTypeFilter.ALL
)

data class EventDescriptionInfo(
    val mainDescription: String,
    val readerInfo: String? = null
)
