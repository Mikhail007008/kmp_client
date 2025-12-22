/**
 * ViewModel для экрана журнала событий системы
 * Управляет загрузкой, фильтрацией и экспортом событий с поддержкой дат, устройств и типов событий
 */
package com.example.kmp_client.presentation.screen.eventLog

import com.example.kmp_client.data.local.DeviceCache
import com.example.kmp_client.data.remote.api.ApiConstants.EVENT_PAGE_SIZE
import com.example.kmp_client.data.remote.dto.request.EventPostRequest
import com.example.kmp_client.data.remote.dto.request.PostFiltersBody
import com.example.kmp_client.data.repository.DevicesRepository
import com.example.kmp_client.data.repository.EventsRepository
import com.example.kmp_client.domain.model.device.Device
import com.example.kmp_client.domain.model.device.DeviceModelType
import com.example.kmp_client.domain.model.device.DeviceOperatingMode
import com.example.kmp_client.domain.model.event.Event
import com.example.kmp_client.domain.model.event.EventFilters
import com.example.kmp_client.domain.model.event.EventTypeFilter
import com.example.kmp_client.domain.util.CsvGenerator
import com.example.kmp_client.domain.util.FileExportHelper
import com.example.kmp_client.domain.util.SaveFileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope


class EventLogViewModel(
    private val eventsRepository: EventsRepository,
    private val devicesRepository: DevicesRepository,
    private val deviceCache: DeviceCache,
    private val fileExportHelper: FileExportHelper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventLogUiState())
    val uiState: StateFlow<EventLogUiState> = _uiState.asStateFlow()

    init {
        loadDevicesForFilterDialog()
    }

    private fun loadDevicesForFilterDialog() {
        viewModelScope.launch {
            val cachedDevices = deviceCache.getDevicesMap()
            val initialDevices = if (cachedDevices.isNotEmpty()) {
                cachedDevices.map { (id, name) ->
                    Device(
                        id = id,
                        name = name,
                        ipAddress = "",
                        port = 0,
                        modelType = DeviceModelType.UNKNOWN,
                        operatingMode = DeviceOperatingMode.UNKNOWN,
                        isSynchronized = false
                    )
                }.sortedBy { it.name }
            } else {
                devicesRepository.fetchDevices().fold(
                    onSuccess = { devices ->
                        deviceCache.saveDevices(devices)
                        devices.sortedBy { it.name }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = "Ошибка при загрузке устройств") }
                        emptyList()
                    }
                )
            }
            _uiState.update {
                val allDeviceIds = initialDevices.map { dev -> dev.id }.toSet()
                it.copy(
                    allAvailableDevices = initialDevices,
                    appliedEventFilters = it.appliedEventFilters.copy(selectedDeviceIds = allDeviceIds),
                    tempEventFilters = it.tempEventFilters.copy(selectedDeviceIds = allDeviceIds)
                )
            }
        }
    }

    fun onDateSelected(type: DatePickerTarget, date: LocalDate) {
        _uiState.update { state ->
            when (type) {
                DatePickerTarget.START -> state.copy(
                    startDate = date,
                    dateTimeValidationError = null
                )

                DatePickerTarget.END -> state.copy(endDate = date, dateTimeValidationError = null)
            }
        }
        validateDateTimeRange()
    }

    fun onTimeSelected(type: TimePickerTarget, time: LocalTime) {
        _uiState.update { state ->
            when (type) {
                TimePickerTarget.START -> state.copy(
                    startTime = time,
                    dateTimeValidationError = null
                )

                TimePickerTarget.END -> state.copy(endTime = time, dateTimeValidationError = null)
            }
        }
        validateDateTimeRange()
    }

    private fun validateDateTimeRange(): Boolean {
        val startDateTime = LocalDateTime(_uiState.value.startDate, _uiState.value.startTime)
        val endDateTime = LocalDateTime(_uiState.value.endDate, _uiState.value.endTime)

        if (startDateTime > endDateTime) {
            _uiState.update { it.copy(dateTimeValidationError = "Начало периода не должно быть позже окончания периода") }
            return false
        }
        _uiState.update { it.copy(dateTimeValidationError = null) }
        return true
    }

    fun loadEvents(isInitialLoad: Boolean = true) {
        if (!validateDateTimeRange()) {
            return
        }

        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMore || (!isInitialLoad && !currentState.canLoadMore)) return

        val pageToLoad = if (isInitialLoad) 0 else currentState.currentPage + 1

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = isInitialLoad,
                    isLoadingMore = !isInitialLoad,
                    error = null
                )
            }

            val allDevicesSelected =
                currentState.appliedEventFilters.selectedDeviceIds.size == currentState.allAvailableDevices.size &&
                        currentState.allAvailableDevices.isNotEmpty()

            val result = if (allDevicesSelected) {
                val apiGetFilters = buildDateTimeFiltersOnly(currentState.appliedEventFilters)
                eventsRepository.getEvents(
                    skip = pageToLoad * EVENT_PAGE_SIZE,
                    limit = EVENT_PAGE_SIZE,
                    filters = apiGetFilters
                )
            } else {
                val postRequestBody = buildPostBody(
                    filters = currentState.appliedEventFilters,
                    skip = pageToLoad * EVENT_PAGE_SIZE,
                    limit = EVENT_PAGE_SIZE
                )
                eventsRepository.getEventsPost(postRequestBody)
            }

            result.fold(
                onSuccess = { paginatedEvents ->
                    _uiState.update {
                        val newEvents =
                            if (isInitialLoad) paginatedEvents.items else it.events + paginatedEvents.items
                        val updatedState = it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            events = newEvents,
                            canLoadMore = paginatedEvents.canLoadMore,
                            currentPage = pageToLoad,
                            totalEventsCount = paginatedEvents.totalCount
                        )

                        updatedState.copy(
                            canSave = newEvents.isNotEmpty() && !updatedState.isLoading && !updatedState.isLoadingMore && updatedState.error == null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false, isLoadingMore = false,
                            error = "Неизвестная ошибка"
                        )
                    }
                }
            )
        }
    }

    private fun buildDateTimeFiltersOnly(
        filters: EventFilters,
    ): Map<String, String>? {
        val finalFilters = mutableMapOf<String, String>()
        var andIndex = 0
        val uiStartDate = _uiState.value.startDate
        val uiStartTime = _uiState.value.startTime
        val uiEndDate = _uiState.value.endDate
        val uiEndTime = _uiState.value.endTime
        val startDateTimeForApi = LocalDateTime(
            date = uiStartDate,
            time = LocalTime(
                hour = uiStartTime.hour,
                minute = uiStartTime.minute,
                second = 0,
                nanosecond = 0
            )
        )
        val endDateTimeForApi = LocalDateTime(
            date = uiEndDate,
            time = LocalTime(
                hour = uiEndTime.hour,
                minute = uiEndTime.minute,
                second = 59,
                nanosecond = 999_000_000
            )
        )
        val startDateTImeApi = startDateTimeForApi.toString() + ":00.000"
        val endDateTImeApi = endDateTimeForApi.toString()

        finalFilters["filters[\$and][${andIndex++}][dateTime][\$gte]"] = startDateTImeApi
        finalFilters["filters[\$and][${andIndex++}][dateTime][\$lte]"] = endDateTImeApi

        if (filters.selectedEventType != EventTypeFilter.ALL && filters.selectedEventType.codes.isNotEmpty()) {
            val eventCodes = filters.selectedEventType.codes

            if (eventCodes.size == 1) {
                val codePair = eventCodes.first()
                finalFilters["filters[\$and][${andIndex++}][code1][\$eq]"] =
                    codePair.code1.toString()
                finalFilters["filters[\$and][${andIndex++}][code2][\$eq]"] =
                    codePair.code2.toString()
            } else {
                var orEventIndex = 0
                for (codePair in eventCodes) {
                    finalFilters["filters[\$and][${andIndex}][\$or][${orEventIndex}][\$and][0][code1][\$eq]"] =
                        codePair.code1.toString()
                    finalFilters["filters[\$and][${andIndex}][\$or][${orEventIndex}][\$and][1][code2][\$eq]"] =
                        codePair.code2.toString()
                    orEventIndex++
                }
                if (orEventIndex > 0) andIndex++
            }
        }

        return finalFilters
    }

    private fun buildPostBody(
        filters: EventFilters,
        skip: Int,
        limit: Int,
    ): EventPostRequest {
        val andConditions = mutableListOf<Map<String, JsonElement>>()
        val json = Json { isLenient = true; ignoreUnknownKeys = true }
        val uiStartDate = _uiState.value.startDate
        val uiStartTime = _uiState.value.startTime
        val uiEndDate = _uiState.value.endDate
        val uiEndTime = _uiState.value.endTime
        val startDateTimeForApi = LocalDateTime(
            date = uiStartDate,
            time = LocalTime(
                hour = uiStartTime.hour,
                minute = uiStartTime.minute,
                second = 0,
                nanosecond = 0
            )
        )
        val endDateTimeForApi = LocalDateTime(
            date = uiEndDate,
            time = LocalTime(
                hour = uiEndTime.hour,
                minute = uiEndTime.minute,
                second = 59,
                nanosecond = 999_000_000
            )
        )

        andConditions.add(
            mapOf(
                "dateTime" to json.encodeToJsonElement(
                    mapOf(
                        "\$gte" to (startDateTimeForApi.toString() + ":00.000"),
                        "\$lte" to endDateTimeForApi.toString()
                    )
                )
            )
        )

        if (filters.selectedDeviceIds.isNotEmpty() &&
            filters.selectedDeviceIds.size != _uiState.value.allAvailableDevices.size
        ) {
            val deviceOrConditions = filters.selectedDeviceIds.map { deviceId ->
                mapOf("device" to json.encodeToJsonElement(mapOf("\$eq" to deviceId)))
            }

            if (deviceOrConditions.isNotEmpty()) {
                andConditions.add(mapOf("\$or" to json.encodeToJsonElement(deviceOrConditions)))
            }
        }

        if (filters.selectedEventType != EventTypeFilter.ALL &&
            filters.selectedEventType.codes.isNotEmpty()
        ) {
            val codePair = filters.selectedEventType.codes.first()

            andConditions.add(
                mapOf("code1" to json.encodeToJsonElement(mapOf("\$eq" to codePair.code1)))
            )
            andConditions.add(
                mapOf("code2" to json.encodeToJsonElement(mapOf("\$eq" to codePair.code2)))
            )
        }

        return EventPostRequest(
            skip = skip,
            limit = limit,
            orderBy = "dateTime:desc",
            filters = PostFiltersBody(`$and` = andConditions)
        )
    }

    fun onOpenFilterDialog() {
        val currentAppliedFilters = _uiState.value.appliedEventFilters
        val allDeviceIds = _uiState.value.allAvailableDevices.map { it.id }.toSet()
        val initialSelectedDevices = if (currentAppliedFilters.selectedDeviceIds.isEmpty()
            && allDeviceIds.isNotEmpty()
        ) {
            allDeviceIds
        } else currentAppliedFilters.selectedDeviceIds

        _uiState.update {
            it.copy(
                showFilterDialog = true,
                tempEventFilters = it.appliedEventFilters.copy(selectedDeviceIds = initialSelectedDevices)
            )
        }
    }

    fun onDeviceSelectionChangedInDialog(deviceId: String, isSelected: Boolean) {
        _uiState.update { state ->
            val currentSelected = state.tempEventFilters.selectedDeviceIds.toMutableSet()

            if (isSelected) {
                currentSelected.add(deviceId)
            } else currentSelected.remove(deviceId)

            state.copy(tempEventFilters = state.tempEventFilters.copy(selectedDeviceIds = currentSelected))
        }
    }

    fun onSelectAllDevicesChanged(selectAll: Boolean) {
        _uiState.update { state ->
            val newSelectedDeviceIds = if (selectAll && state.allAvailableDevices.isNotEmpty()) {
                state.allAvailableDevices.map { it.id }.toSet()
            } else emptySet()

            state.copy(tempEventFilters = state.tempEventFilters.copy(selectedDeviceIds = newSelectedDeviceIds))
        }
    }

    fun onEventTypeSelectedInDialog(eventTypeDisplayName: String) {
        EventTypeFilter.fromDisplayName(eventTypeDisplayName)?.let { selectedType ->
            _uiState.update { state ->
                state.copy(tempEventFilters = state.tempEventFilters.copy(selectedEventType = selectedType))
            }
        }
    }

    fun onDismissFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = false) }
    }

    fun onApplyFilters() {
        _uiState.update {
            it.copy(
                showFilterDialog = false,
                appliedEventFilters = it.tempEventFilters
            )
        }
        loadEvents(isInitialLoad = true)
    }

    fun onCLearFiltersInDialog() {
        val allDeviceIds = _uiState.value.allAvailableDevices.map { it.id }.toSet()
        _uiState.update {
            it.copy(
                tempEventFilters = EventFilters(
                    selectedDeviceIds = if (allDeviceIds.isNotEmpty()) allDeviceIds else emptySet(),
                    selectedEventType = EventTypeFilter.ALL
                )
            )
        }
    }

    fun showPicker(target: Any) {
        _uiState.update { state ->
            when (target) {
                is DatePickerTarget -> state.copy(isDatePickerVisible = target)
                is TimePickerTarget -> state.copy(isTimePickerVisible = target)
                else -> state
            }
        }
    }

    fun hidePicker(type: PickerType) {
        _uiState.update { state ->
            when (type) {
                PickerType.DATE -> state.copy(isDatePickerVisible = null)
                PickerType.TIME -> state.copy(isTimePickerVisible = null)
            }
        }
    }

    fun exportEventsToCsv() {
        val currentState = _uiState.value
        if (currentState.isExporting || currentState.events.isEmpty()) return

        exportEventsToCsvWithSuggestedName()
    }

    private suspend fun loadAllEventsForExport(): List<Event> {
        val currentState = _uiState.value
        val allDevicesSelected =
            currentState.appliedEventFilters.selectedDeviceIds.size == currentState.allAvailableDevices.size &&
                    currentState.allAvailableDevices.isNotEmpty()

        val filters = if (allDevicesSelected) {
            buildDateTimeFiltersOnly(currentState.appliedEventFilters)
        } else {
            null
        }

        // Определяем сколько событий нужно загрузить
        val totalEventsToFetch = minOf(currentState.totalEventsCount, MAX_EXPORT_ROWS)
        if (totalEventsToFetch == 0) return emptyList()

        val allEvents = mutableListOf<Event>()

        // Рассчитываем количество страниц
        val totalPages = (totalEventsToFetch + EVENT_PAGE_SIZE - 1) / EVENT_PAGE_SIZE

        for (currentPage in 0 until totalPages) {
            val remainingEvents = totalEventsToFetch - allEvents.size
            val pageSize = minOf(EVENT_PAGE_SIZE, remainingEvents)

            val result = if (allDevicesSelected) {
                eventsRepository.getEvents(
                    skip = currentPage * EVENT_PAGE_SIZE,
                    limit = pageSize,
                    filters = filters
                )
            } else {
                val postRequestBody = buildPostBody(
                    filters = currentState.appliedEventFilters,
                    skip = currentPage * EVENT_PAGE_SIZE,
                    limit = pageSize
                )
                eventsRepository.getEventsPost(postRequestBody)
            }

            result.fold(
                onSuccess = { paginatedEvents ->
                    val newEvents = paginatedEvents.items
                    allEvents.addAll(newEvents)

                    if (newEvents.size < pageSize && allEvents.size < totalEventsToFetch) {
                        throw Exception("Сервер вернул неполные данные: ожидалось $pageSize, получено ${newEvents.size}")
                    }
                },
                onFailure = { error ->
                    throw Exception("Ошибка при загрузке событий")
                }
            )
        }

        return allEvents
    }

    private fun exportEventsToCsvWithSuggestedName() {
        val currentState = _uiState.value
        if (currentState.isExporting || currentState.events.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportError = null
                )
            }

            try {
                val eventsToExport = loadAllEventsForExport()

                if (eventsToExport.size > MAX_EXPORT_ROWS) {
                    throw IllegalStateException("Количество событий (${eventsToExport.size}) превышает максимально допустимое (${MAX_EXPORT_ROWS})")
                }

                val csvData = createCsvData(eventsToExport)
                val suggestedFileName = fileExportHelper.generateFileName("event_log", "csv")

                val saveData = SaveFileData(
                    data = csvData,
                    fileName = suggestedFileName,
                    mimeType = "text/csv"
                )

                fileExportHelper.showSaveDialog(saveData) { result ->
                    _uiState.update {
                        if (result.success) {
                            it.copy(
                                isExporting = false,
                                exportError = "Отчет успешно сохранен${result.filePath?.let { " в: $it" } ?: ""}"
                            )
                        } else {
                            it.copy(
                                isExporting = false,
                                exportError = result.error
                                    ?: "Неизвестная ошибка при сохранении"
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportError = "Ошибка при экспорте"
                    )
                }
            }
        }
    }

    private fun createCsvData(events: List<Event>): ByteArray {
        return CsvGenerator.generateCsvBytes(events, MAX_EXPORT_ROWS)
    }

    companion object {
        private const val MAX_EXPORT_ROWS = 1000
    }
}