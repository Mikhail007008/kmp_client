package com.example.kmp_client.presentation.screen.users

import com.example.kmp_client.domain.model.user.User

data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val showFilterDialog: Boolean = false,
    val error: String? = null,
    val canLoadMore: Boolean = true,
    val totalUsers: Int = 0,
    val currentPage: Int = 0,
    val isLoadingDictionaries: Boolean = false,
    val dictionariesError: String? = null,
    val jobTitlesMap: Map<Long, String> = emptyMap(),
    val departmentsMap: Map<Long, String> = emptyMap(),
    val areFiltersActive: Boolean = false,
    val showAddUserDialog: Boolean = false,
    val isAddingUser: Boolean = false,
    val addUserError: String? = null,
    val successMessage: String? = null,
    val isSelectionModeActive: Boolean = false,
    val selectedUserIds: Set<Long> = emptySet(),
    val selectionActionError: String? = null,
    val showConfirmDeleteDialog: Boolean = false,
    val usersToDeleteCount: Int = 0,
    val deletionInProgress: Boolean = false,
    val deletionError: String? = null,
    val discoveredFeatureKeys: List<String> = emptyList(),
    val hasExtraFeatures: Boolean = false
)