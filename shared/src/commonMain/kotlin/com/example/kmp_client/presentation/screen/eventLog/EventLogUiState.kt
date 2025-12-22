package com.example.kmp_client.presentation.screen.eventLog

import com.example.kmp_client.domain.model.device.Device
import com.example.kmp_client.domain.model.event.Event
import com.example.kmp_client.domain.model.event.EventFilters
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

enum class DatePickerTarget { START, END }
enum class TimePickerTarget { START, END }
enum class PickerType { DATE, TIME }

data class EventLogUiState(
    val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val startTime: LocalTime = LocalTime(0, 0),
    val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val endTime: LocalTime = LocalTime(23, 59),
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val dateTimeValidationError: String? = null,
    val canLoadMore: Boolean = false,
    val currentPage: Int = 0,
    val totalEventsCount: Int = 0,
    val isDatePickerVisible: DatePickerTarget? = null,
    val isTimePickerVisible: TimePickerTarget? = null,
    val showFilterDialog: Boolean = false,
    val appliedEventFilters: EventFilters = EventFilters(),
    val tempEventFilters: EventFilters = EventFilters(),
    val allAvailableDevices: List<Device> = emptyList(),
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val canSave: Boolean = false
)