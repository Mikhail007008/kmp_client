package com.example.kmp_client.presentation.screen.key

import com.example.kmp_client.domain.model.key.Key

data class KeysUiState(
    val keys: List<Key> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val totalKeys: Int = 0,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 0,
    val successMessage: String? = null,
    val areFiltersActive: Boolean = false,
    val isSelectionModeActive: Boolean = false,
    val selectedItemIds: Set<String> = emptySet(),
    val showConfirmDeleteDialog: Boolean = false,
    val itemsToDeleteCount: Int = 0,
    val isSelectionActionInProgress: Boolean = false,
    val selectionActionError: String? = null,
    val showFilterDialog: Boolean = false,
    val showAddKeyDialog: Boolean = false
)