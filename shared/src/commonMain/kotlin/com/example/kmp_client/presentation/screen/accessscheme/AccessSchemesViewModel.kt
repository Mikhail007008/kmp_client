/**
 * ViewModel для экрана схем доступа
 * Загружает и сортирует список схем доступа
 */
package com.example.kmp_client.presentation.screen.accessscheme

import com.example.kmp_client.data.repository.AccessSchemesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class AccessSchemesViewModel(
    private val accessSchemesRepository: AccessSchemesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccessSchemesUIState())
    val uiState: StateFlow<AccessSchemesUIState> = _uiState.asStateFlow()

    init {
        loadAccessSchemes()
    }

    fun loadAccessSchemes() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = accessSchemesRepository.getAccessSchemes()
            result.fold(
                onSuccess = { schemes ->
                    val sortedSchemes =
                        schemes.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            accessSchemes = sortedSchemes,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки схем доступа",
                            accessSchemes = emptyList()
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