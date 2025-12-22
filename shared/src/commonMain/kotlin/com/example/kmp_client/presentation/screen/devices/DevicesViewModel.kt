/**
 * ViewModel для экрана управления устройствами
 * Загружает и кеширует список устройств системы с сортировкой по имени
 */
package com.example.kmp_client.presentation.screen.devices

import com.example.kmp_client.data.local.DeviceCache
import com.example.kmp_client.data.repository.DevicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class DevicesViewModel(
    private val devicesRepository: DevicesRepository,
    private val deviceCache: DeviceCache
) : ViewModel() {
    private val _uiState = MutableStateFlow(DevicesUIState())
    val uiState: StateFlow<DevicesUIState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            devicesRepository.fetchDevices().fold(
                onSuccess = { devices ->
                    deviceCache.saveDevices(devices)

                    val sortedDevices = devices.sortedBy { it.name }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            devices = sortedDevices,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки устройств",
                            devices = emptyList()
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}