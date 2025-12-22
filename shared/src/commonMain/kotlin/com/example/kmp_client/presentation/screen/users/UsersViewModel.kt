/**
 * ViewModel для экрана управления пользователями
 * Управляет загрузкой, фильтрацией, поиском, созданием и удалением пользователей
 */
package com.example.kmp_client.presentation.screen.users

import com.example.kmp_client.core.network.NetworkException
import com.example.kmp_client.data.remote.api.ApiConstants.MAX_SELECTION_COUNT
import com.example.kmp_client.data.remote.api.ApiConstants.PAGE_SIZE
import com.example.kmp_client.data.remote.dto.request.UserCreation
import com.example.kmp_client.data.remote.dto.request.UserCreationData
import com.example.kmp_client.data.remote.dto.response.Department
import com.example.kmp_client.data.repository.UsersRepository
import com.example.kmp_client.domain.model.filter.FieldFilterState
import com.example.kmp_client.domain.model.filter.FilterOperator
import com.example.kmp_client.domain.model.user.UserFilterModels
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope


class UsersViewModel(
    private val usersRepository: UsersRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private val _userFilterModels = MutableStateFlow(UserFilterModels())
    val userFilterModels: StateFlow<UserFilterModels> = _userFilterModels.asStateFlow()

    private val _discoveredFeatureKeys = MutableStateFlow<List<String>>(emptyList())
    val discoveredFeatureKeys: StateFlow<List<String>> = _discoveredFeatureKeys.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UserScreenUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDictionaries = true, dictionariesError = null) }

            val jobTitlesResult = usersRepository.getJobTitles()
            val departmentsResult = usersRepository.getDepartments()

            var allSuccess = true
            var errorMessages = ""
            var tempJobTitlesMap: Map<Long, String> = emptyMap()
            var tempDepartmentsMap: Map<Long, String> = emptyMap()

            jobTitlesResult
                .onSuccess { titles -> tempJobTitlesMap = titles.associate { it.id to it.name } }
                .onFailure { error ->
                    errorMessages += "Ошибка загрузки должностей"
                    allSuccess = false
                }

            departmentsResult
                .onSuccess { deps -> tempDepartmentsMap = processDepartments(deps) }
                .onFailure { error ->
                    errorMessages += "Ошибка загрузки подразделений"
                    allSuccess = false
                }

            _uiState.update {
                it.copy(
                    isLoadingDictionaries = false,
                    dictionariesError = if (errorMessages.isNotBlank()) errorMessages.trim() else null,
                    jobTitlesMap = if (allSuccess || tempJobTitlesMap.isNotEmpty()) tempJobTitlesMap else it.jobTitlesMap,
                    departmentsMap = if (allSuccess || tempDepartmentsMap.isNotEmpty()) tempDepartmentsMap else it.departmentsMap
                )
            }

            if (allSuccess || tempJobTitlesMap.isNotEmpty() || tempDepartmentsMap.isNotEmpty()) {
                loadInitialUsers()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        areFiltersActive = _userFilterModels.value.hasActiveFilters()
                    )
                }
            }
        }
    }

    private fun processDepartments(deps: List<Department>): Map<Long, String> {
        val departmentsMap = mutableMapOf<Long, String>()
        fun buildDepartmentHierarchyFlatMap(departmentList: List<Department>) {
            departmentList.forEach { department ->
                departmentsMap[department.id] = department.name
                if (department.children.isNotEmpty()) {
                    buildDepartmentHierarchyFlatMap(department.children)
                }
            }
        }
        buildDepartmentHierarchyFlatMap(deps)
        return departmentsMap
    }

    fun loadInitialUsers(filtersApplied: Boolean? = null) {
        if (filtersApplied != null) {
            _uiState.update { it.copy(isLoading = true) }
        }

        resetSelectionMode()

        viewModelScope.launch {
            if (!_uiState.value.isLoading) {
                _uiState.update { it.copy(isLoading = true) }
            }
            _uiState.update {
                it.copy(
                    isLoading = true,
                    users = emptyList(),
                    currentPage = 0,
                    canLoadMore = true,
                    error = null,
                    areFiltersActive = filtersApplied ?: _userFilterModels.value.hasActiveFilters()
                )
            }

            val queryFilters =
                _userFilterModels.value.takeIf { it.hasActiveFilters() }?.toQueryMap()

            usersRepository.getUsers(
                skip = 0,
                limit = PAGE_SIZE,
                filters = queryFilters
            )
                .onSuccess { paginatedResult ->
                    val newDiscoveredKeys =
                        paginatedResult.discoveredFeatureKeys.distinct().sorted()

                    if (_discoveredFeatureKeys.value.toSet() != newDiscoveredKeys.toSet()) {
                        _discoveredFeatureKeys.value = newDiscoveredKeys
                    }

                    _userFilterModels.update { currentFilters ->
                        val standardFeatureKeys =
                            setOf("feature1", "feature2", "feature3", "feature4")
                        val allFilterableFeatureKeys =
                            standardFeatureKeys + newDiscoveredKeys.filter { key ->
                                key.startsWith("feature") && key.removePrefix("feature")
                                    .toIntOrNull() in 1..4
                            }

                        val updateFeatureFilters = allFilterableFeatureKeys.map { key ->
                            currentFilters.featuresFilters.find { it.fieldName == key }?.copy(
                                displayName = "Свойство ${key.removePrefix("feature")}"
                            ) ?: FieldFilterState(
                                fieldName = key,
                                displayName = "Свойство ${key.removePrefix("feature")}",
                                selectedOperator = FilterOperator.EQUALS,
                                availableOperators = FilterOperator.stringOperators()
                            )
                        }
                        currentFilters.copy(featuresFilters = updateFeatureFilters)
                    }

                    val standardFeatureKeys = setOf("feature1", "feature2", "feature3", "feature4")
                    val hasExtra = newDiscoveredKeys.any { it !in standardFeatureKeys }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = paginatedResult.users,
                            totalUsers = paginatedResult.totalCount,
                            canLoadMore = paginatedResult.canLoadMore,
                            currentPage = 1,
                            areFiltersActive = _userFilterModels.value.hasActiveFilters(),
                            discoveredFeatureKeys = newDiscoveredKeys,
                            hasExtraFeatures = hasExtra
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Неизвестная ошибка загрузки пользователя"
                        )
                    }
                }
        }
    }

    fun refreshUsersList() {
        resetSelectionMode()
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    error = null,
                    users = emptyList(),
                    currentPage = 0,
                    canLoadMore = true
                )
            }

            val queryFilters =
                _userFilterModels.value.takeIf { it.hasActiveFilters() }?.toQueryMap()

            usersRepository.getUsers(
                skip = 0,
                limit = PAGE_SIZE,
                filters = queryFilters
            )
                .onSuccess { paginatedResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = paginatedResult.users,
                            totalUsers = paginatedResult.totalCount,
                            canLoadMore = paginatedResult.canLoadMore,
                            currentPage = 1,
                            areFiltersActive = _userFilterModels.value.hasActiveFilters()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка обновления списка пользователей"
                        )
                    }
                }
        }
    }


    fun loadMoreUsers() {
        val currentState = _uiState.value

        if (currentState.isLoadingMore || !currentState.canLoadMore || currentState.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            val currentSkip = currentState.currentPage * PAGE_SIZE
            val queryFilters =
                _userFilterModels.value.takeIf { it.hasActiveFilters() }?.toQueryMap()

            usersRepository.getUsers(
                skip = currentSkip,
                limit = PAGE_SIZE,
                filters = queryFilters
            )
                .onSuccess { paginatedResult ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            users = it.users + paginatedResult.users,
                            totalUsers = paginatedResult.totalCount,
                            canLoadMore = paginatedResult.canLoadMore,
                            currentPage = it.currentPage + 1
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message ?: "Ошибка при загрузке следующей страницы"
                        )
                    }
                }
        }
    }

    fun onFilterButtonClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(showFilterDialog = true) }
        }
    }

    fun onFilterDialogCancelledAndDismissed() {
        _userFilterModels.value = UserFilterModels()
        _uiState.update { it.copy(showFilterDialog = false) }
        loadInitialUsers(filtersApplied = false)
    }

    fun onApplyFiltersClicked(updatedFilters: UserFilterModels) {
        _userFilterModels.value = updatedFilters
        _uiState.update { it.copy(showFilterDialog = false) }
        loadInitialUsers(filtersApplied = updatedFilters.hasActiveFilters())
    }

    fun onClearFiltersInDialogRequested() {
        val standardFeatureKeys = setOf("feature1", "feature2", "feature3", "feature4")
        val allFilterableFeatureKeys =
            standardFeatureKeys + _discoveredFeatureKeys.value.filter { key ->
                key.startsWith("feature") && key.removePrefix("feature").toIntOrNull() in 1..4
            }

        _userFilterModels.value = UserFilterModels(
            featuresFilters = allFilterableFeatureKeys.map { key ->
                FieldFilterState(
                    fieldName = key,
                    displayName = "Свойство ${key.removePrefix("feature")}",
                    selectedOperator = FilterOperator.EQUALS,
                    availableOperators = FilterOperator.stringOperators()
                )
            }
        )
    }

    fun onAddUserClicked() {
        _uiState.update { it.copy(showAddUserDialog = true, addUserError = null) }
    }

    fun onAddUserDialogDismissed() {
        _uiState.update { it.copy(showAddUserDialog = false, addUserError = null) }
    }

    fun addUser(userData: UserCreationData) {
        if (_uiState.value.isAddingUser) return
        resetSelectionMode()

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingUser = true, addUserError = null, error = null) }

            val userLoad = UserCreation(users = listOf(userData))

            usersRepository.createUser(userLoad)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isAddingUser = false,
                            showAddUserDialog = false,
                            successMessage = "Пользователь успешно добавлен",
                            error = null,
                            addUserError = null
                        )
                    }
                    loadInitialUsers()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAddingUser = false,
                            addUserError = error.message ?: "Ошибка при добавлении пользователя"
                        )
                    }
                }
        }
    }

    fun onApplyInSelectionModeClicked() {
        if (!_uiState.value.isSelectionModeActive) return
        val selectedIds = _uiState.value.selectedUserIds

        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(selectionActionError = "Не выбрано ни одного пользователя") }
            return
        }
        _uiState.update {
            it.copy(
                showConfirmDeleteDialog = true,
                usersToDeleteCount = selectedIds.size,
                selectionActionError = null
            )
        }
    }

    fun onConfirmDeletionDialog() {
        if (!_uiState.value.isSelectionModeActive || _uiState.value.selectedUserIds.isEmpty()) return
        _uiState.update { it.copy(showConfirmDeleteDialog = false) }
        proceedWithDeletion()
    }

    fun toggleUserSelection(userId: Long) {
        if (!_uiState.value.isSelectionModeActive) return

        _uiState.update { currentState ->
            val currentSelectedIds = currentState.selectedUserIds.toMutableSet()

            if (currentSelectedIds.contains(userId)) {
                currentSelectedIds.remove(userId)
                currentState.copy(selectedUserIds = currentSelectedIds, selectionActionError = null)
            } else {
                if (currentSelectedIds.size < MAX_SELECTION_COUNT) {
                    currentSelectedIds.add(userId)
                    currentState.copy(
                        selectedUserIds = currentSelectedIds,
                        selectionActionError = null
                    )
                } else {
                    viewModelScope.launch {
                        _uiEvent.emit(UserScreenUiEvent.ShowSnackbar("Можно выбрать не более $MAX_SELECTION_COUNT пользователей"))
                    }
                    currentState
                }
            }
        }
    }

    private fun proceedWithDeletion() {
        val idsToDelete = _uiState.value.selectedUserIds
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(deletionInProgress = true, deletionError = null) }

            val deletionResults = mutableListOf<Pair<Long, Result<Unit>>>()

            idsToDelete.forEach { userId ->
                val result = usersRepository.deleteUser(userId)
                deletionResults.add(userId to result)
            }

            val failedDeletions = deletionResults.filter { it.second.isFailure }

            if (failedDeletions.isEmpty()) {
                _uiState.update {
                    it.copy(
                        deletionInProgress = false,
                        isSelectionModeActive = false,
                        selectedUserIds = emptySet()
                    )
                }
                viewModelScope.launch { _uiEvent.emit(UserScreenUiEvent.ShowSnackbar("Пользователи успешно удалены")) }
                refreshUsersList()
            } else {
                val errorMessages = failedDeletions.map { (userId, result) ->
                    val exception = result.exceptionOrNull()
                    val specificMessage = when (exception) {
                        is NetworkException.ServerException -> "ID: ${userId}, Ошибка удаления"
                        is NetworkException -> "ID: ${userId}, Ошибка удаления"
                        else -> "ID ${userId}: Неизвестная ошибка"
                    }
                    specificMessage
                }
                _uiState.update {
                    it.copy(
                        deletionInProgress = false,
                        deletionError = "Не удалось удалить некоторых пользователей:\n${
                            errorMessages.joinToString(
                                "\n"
                            )
                        }"
                    )
                }
                viewModelScope.launch {
                    _uiEvent.emit(
                        UserScreenUiEvent.ShowErrorDialog(
                            "Ошибка удаления",
                            errorMessages
                        )
                    )
                }
                refreshUsersList()
            }
        }
    }

    fun onDeleteButtonClicked() {
        val currentSelectionMode = _uiState.value.isSelectionModeActive
        if (currentSelectionMode) {
            resetSelectionMode()
        } else {
            _uiState.update { it.copy(isSelectionModeActive = true, selectionActionError = null) }
        }
    }

    fun onCancelInSelectionModeClicked() {
        if (!_uiState.value.isSelectionModeActive) return
        _uiState.update {
            it.copy(
                selectedUserIds = emptySet(),
                selectionActionError = null
            )
        }
    }

    private fun resetSelectionMode() {
        _uiState.update {
            it.copy(
                isSelectionModeActive = false,
                selectedUserIds = emptySet(),
                showConfirmDeleteDialog = false,
                deletionInProgress = false,
                deletionError = null,
                selectionActionError = null
            )
        }
    }

    fun onDismissDeletionDialog() {
        _uiState.update { it.copy(showConfirmDeleteDialog = false) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, dictionariesError = null) }
    }

    fun clearSelectionActionError() {
        _uiState.update { it.copy(selectionActionError = null) }
    }
}