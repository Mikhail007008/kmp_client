/**
 * ViewModel для экрана управления ключами доступа
 * Управляет загрузкой, созданием, фильтрацией и удалением ключей
 */
package com.example.kmp_client.presentation.screen.key

import com.example.kmp_client.core.network.NetworkException
import com.example.kmp_client.data.remote.api.ApiConstants.MAX_SELECTION_COUNT
import com.example.kmp_client.data.remote.api.ApiConstants.PAGE_SIZE
import com.example.kmp_client.data.remote.dto.request.KeyCreationRequest
import com.example.kmp_client.data.repository.KeysRepository
import com.example.kmp_client.domain.model.key.AddKeyDialogState
import com.example.kmp_client.domain.model.key.KeyFiltersModels
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class KeysViewModel(
    private val keysRepository: KeysRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(KeysUiState())

    val uiState: StateFlow<KeysUiState> = _uiState.asStateFlow()
    private val _addKeyDialogState = MutableStateFlow(AddKeyDialogState())

    val addKeyDialogState: StateFlow<AddKeyDialogState> = _addKeyDialogState.asStateFlow()
    private var _isAddingKey = MutableStateFlow(false)

    val isAddingKey: StateFlow<Boolean> = _isAddingKey.asStateFlow()
    private val _activeFilters = MutableStateFlow(KeyFiltersModels())

    val activeFilters: StateFlow<KeyFiltersModels> = _activeFilters.asStateFlow()
    private val _uiEvent = MutableSharedFlow<KeyScreenUiEvent>()

    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadInitialKeys()
    }

    fun loadInitialKeys(filtersStatusUpdated: Boolean? = null) {
        viewModelScope.launch {

            if (!_uiState.value.isLoading) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(error = null) }
            }

            _uiState.update {
                it.copy(
                    keys = emptyList(),
                    currentPage = 0,
                    canLoadMore = true,
                    areFiltersActive = filtersStatusUpdated
                        ?: _activeFilters.value.hasActiveFilters()
                )
            }

            val queryFilters = _activeFilters.value.takeIf { it.hasActiveFilters() }?.toQueryMap()

            keysRepository.getKeys(
                skip = 0,
                limit = PAGE_SIZE,
                filters = queryFilters
            ).onSuccess { paginatedResult ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        keys = paginatedResult.items,
                        totalKeys = paginatedResult.totalCount,
                        canLoadMore = paginatedResult.canLoadMore,
                        currentPage = if (paginatedResult.items.isNotEmpty()) 1 else 0
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Ошибка загрузки ключей"
                    )
                }
            }
        }
    }

    fun refreshKeysList() {
        loadInitialKeys(filtersStatusUpdated = _activeFilters.value.hasActiveFilters())
    }

    fun loadMoreKeys() {
        val currentState = _uiState.value

        if (currentState.isLoadingMore || !currentState.canLoadMore || currentState.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            val currentSkip = currentState.currentPage * PAGE_SIZE
            val queryFilters = _activeFilters.value.takeIf { it.hasActiveFilters() }?.toQueryMap()

            keysRepository.getKeys(
                skip = currentSkip,
                limit = PAGE_SIZE,
                filters = queryFilters
            ).onSuccess { paginatedResult ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        keys = it.keys + paginatedResult.items,
                        totalKeys = paginatedResult.totalCount,
                        canLoadMore = paginatedResult.canLoadMore,
                        currentPage = it.currentPage + if (paginatedResult.items.isNotEmpty()) 1 else 0
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = exception.message ?: "Ошибка при загрузке следующей страницы"
                    )
                }
            }
        }
    }

    fun onAddKeyClicked() {
        _addKeyDialogState.value = AddKeyDialogState()
        _uiState.update { it.copy(showAddKeyDialog = true) }
    }

    fun onAddKeyDialogDismissed() {
        _uiState.update { it.copy(showAddKeyDialog = false) }
        _isAddingKey.value = false
    }

    fun onAddKeyStateChanged(newState: AddKeyDialogState) {
        _addKeyDialogState.value = newState
    }

    fun onConfirmAddKey() {
        val currentDialogState = _addKeyDialogState.value
        if (currentDialogState.uid.value.isBlank()) {
            _addKeyDialogState.update {
                it.copy(
                    uid = it.uid.copy("UID не может быть пустым"),
                    overallErrorMessage = it.overallErrorMessage
                        ?: "Исправьте ошибки заполненых форм"
                )
            }
            _isAddingKey.value = false
            return
        }

        viewModelScope.launch {
            _isAddingKey.value = true

            val expirationIsoDateString =
                currentDialogState.expiration.value.takeIf { it.isNotBlank() }
            val expirationDateTimeStringForServer: String? =
                expirationIsoDateString?.let { datePart ->
                    "${datePart}T23:59:59"
                }

            val requestData = KeyCreationRequest(
                id = currentDialogState.uid.value,
                expiration = expirationDateTimeStringForServer,
                reentryAlwaysAllowed = currentDialogState.reentryAlwaysAllowed.value,
                extCode = if (currentDialogState.extCode.selectionOptions.isNotEmpty()) currentDialogState.extCode.calculatedValue else null,
                description = currentDialogState.description.value.takeIf { it.isNotBlank() }
            )

            keysRepository.createKey(requestData)
                .onSuccess {
                    _uiEvent.emit(KeyScreenUiEvent.ShowSnackbar("Ключ успешно добавлен"))
                    onAddKeyDialogDismissed()
                    refreshKeysList()
                }
                .onFailure { exception ->
                    _addKeyDialogState.update {
                        it.copy(
                            overallErrorMessage = exception.message ?: "Ошибка при создании ключа"
                        )
                    }
                }

            if (_uiState.value.showAddKeyDialog || !_isAddingKey.value) _isAddingKey.value = false
        }
    }

    fun onFilterKeysClicked() {
        _uiState.update { it.copy(showFilterDialog = true) }
    }

    fun onFilterKeysDismissed() {
        _uiState.update { it.copy(showFilterDialog = false) }
    }

    fun onApplyFiltersClicked(updatedFilters: KeyFiltersModels) {
        _activeFilters.value = updatedFilters
        _uiState.update { it.copy(showFilterDialog = false) }
        loadInitialKeys(filtersStatusUpdated = updatedFilters.hasActiveFilters())
    }

    fun onClearFiltersInDialog() {
        _activeFilters.value = KeyFiltersModels()
    }

    fun onFilterDialogCancelled() {
        onClearFiltersInDialog()
        _uiState.update { it.copy(showFilterDialog = false) }
        loadInitialKeys(filtersStatusUpdated = false)
    }

    fun onToggleKeySelectionMode() {
        val currentMode = _uiState.value.isSelectionModeActive
        _uiState.update {
            it.copy(
                isSelectionModeActive = !currentMode,
                selectedItemIds = if (!currentMode) it.selectedItemIds else emptySet()
            )
        }
    }

    fun toggleKeySelection(keyId: String) {
        if (!_uiState.value.isSelectionModeActive) return

        _uiState.update { currentState ->
            val currentSelectedIds = currentState.selectedItemIds.toMutableSet()
            if (currentSelectedIds.contains(keyId)) {
                currentSelectedIds.remove(keyId)
            } else {
                if (currentSelectedIds.size < MAX_SELECTION_COUNT) {
                    currentSelectedIds.add(keyId)
                } else viewModelScope.launch { _uiEvent.emit(KeyScreenUiEvent.ShowSnackbar("Можно выбрать не более 10 ключей")) }
            }
            currentState.copy(selectedItemIds = currentSelectedIds, selectionActionError = null)
        }
    }

    fun onApplyKeySelectionAction() {
        if (!_uiState.value.isSelectionModeActive) return
        val selectedIds = _uiState.value.selectedItemIds

        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(selectionActionError = "Не выбрано ни одного ключа") }
            return
        }

        _uiState.update {
            it.copy(
                showConfirmDeleteDialog = true,
                itemsToDeleteCount = selectedIds.size,
                selectionActionError = null
            )
        }
    }

    fun onConfirmDeletionDialog() {
        if (!_uiState.value.isSelectionModeActive || _uiState.value.selectedItemIds.isEmpty()) return
        _uiState.update { it.copy(showConfirmDeleteDialog = false) }
        proceedWithKeyDeletion()
    }

    private fun proceedWithKeyDeletion() {
        val idsToDelete = _uiState.value.selectedItemIds
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSelectionActionInProgress = true, selectionActionError = null) }

            val deletionResults = mutableListOf<Pair<String, Result<Unit>>>()

            idsToDelete.forEach { keyId ->
                val result = keysRepository.deleteKey(keyId)
                deletionResults.add(keyId to result)
            }

            val failedDeletions = deletionResults.filter { it.second.isFailure }

            if (failedDeletions.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isSelectionActionInProgress = false,
                        isSelectionModeActive = false,
                        selectedItemIds = emptySet()
                    )
                }

                _uiEvent.emit(KeyScreenUiEvent.ShowSnackbar("Ключи успешно удалены"))
                refreshKeysList()
            } else {
                val errorMessage = failedDeletions.map { (keyId, result) ->
                    val exception = result.exceptionOrNull()
                    when (exception) {
                        is NetworkException.ServerException -> "UID: $keyId, Ошибка удаления"
                        is NetworkException -> "UID: $keyId, Ошибка удаления"
                        else -> "UID $keyId: Неизвестная ошибка"
                    }
                }
                _uiState.update {
                    it.copy(
                        isSelectionActionInProgress = false,
                        selectionActionError = "Не удалось удалить нектороые ключи:\n${errorMessage.joinToString("\n")}"
                    )
                }
                refreshKeysList()
            }
        }
    }

    fun onDismissDeletionDialog() {
        _uiState.update { it.copy(showConfirmDeleteDialog = false) }
    }

    fun onCancelKeySelection() {
        if (!_uiState.value.isSelectionModeActive) return
        _uiState.update {
            it.copy(
                selectedItemIds = emptySet(),
                selectionActionError = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearSelectionActionError() {
        _uiState.update { it.copy(selectionActionError = null) }
    }

    sealed class KeyScreenUiEvent {
        data class ShowSnackbar(val message: String) : KeyScreenUiEvent()
    }
}