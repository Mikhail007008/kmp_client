package com.example.kmp_client.presentation.screen.key

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.key.Key
import com.example.kmp_client.presentation.ui.component.CustomVerticalScrollbarForLazyList
import com.example.kmp_client.presentation.ui.component.ScreenBottomAppBar
import kotlinx.coroutines.launch
import moe.tlaster.precompose.koin.koinViewModel
import com.example.kmp_client.presentation.ui.theme.*
import kmp_eraclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun KeysScreen(
    snackbarHostState: SnackbarHostState,
) {
    val viewModel: KeysViewModel = koinViewModel(KeysViewModel::class)
    val uiState by viewModel.uiState.collectAsState()
    val currentKeyFilters by viewModel.activeFilters.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val addKeyDialogState by viewModel.addKeyDialogState.collectAsState()
    val isAddingKey by viewModel.isAddingKey.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is KeysViewModel.KeyScreenUiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            ScreenBottomAppBar(
                onFilterClick = {
                    if (!uiState.isSelectionModeActive) viewModel.onFilterKeysClicked()
                },
                areFiltersActive = uiState.areFiltersActive,
                onAddItemClick = { viewModel.onAddKeyClicked() },
                onRefreshClick = { viewModel.refreshKeysList() },
                isRefreshing = uiState.isLoading,
                isSelectionModeActive = uiState.isSelectionModeActive,
                selectedItemsCount = uiState.selectedItemIds.size,
                onDeleteModeToggleClick = { viewModel.onToggleKeySelectionMode() },
                onCancelSelectionClick = { viewModel.onCancelKeySelection() },
                onApplySelectionClick = { viewModel.onApplyKeySelectionAction() },
                isDeletionInProgress = uiState.isSelectionActionInProgress
            )
        }
    ) { paddingValues ->
        LaunchedEffect(uiState.error, uiState.successMessage, uiState.selectionActionError) {
            uiState.error?.let {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearError()
                }
            }
            uiState.successMessage?.let {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearSuccessMessage()
                }
            }
            uiState.selectionActionError?.let {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearSelectionActionError()
                }
            }
        }

        if (uiState.showConfirmDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeletionDialog() },
                title = { Text(stringResource(Res.string.delete_confirmation)) },
                text = { Text("Подтвердите удаление ${uiState.itemsToDeleteCount} ключ${if (uiState.itemsToDeleteCount > 1) "ей" else "а"}") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onConfirmDeletionDialog() }) {
                        Text(stringResource(Res.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDeletionDialog() }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        focusManager.clearFocus()
                    }
                )
        ) {
            val containerHeight = maxHeight

            if (uiState.isLoading && uiState.keys.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                KeysList(
                    keys = uiState.keys,
                    isLoadingMore = uiState.isLoadingMore,
                    canLoadMore = uiState.canLoadMore,
                    onLoadMore = { viewModel.loadMoreKeys() },
                    onItemClick = { keyId ->
                        if (uiState.isSelectionModeActive) {
                            viewModel.toggleKeySelection(keyId)
                        }
                    },
                    isSelectionModeActive = uiState.isSelectionModeActive,
                    selectedItemIds = uiState.selectedItemIds,
                    containerHeight = containerHeight,
                    error = uiState.error,
                    isLoadingInitial = uiState.isLoading && uiState.keys.isEmpty()
                )
            }
        }

        if (uiState.showFilterDialog) {
            KeyFilterDialog(
                activeKeyFilters = currentKeyFilters,
                onDismissRequest = { viewModel.onFilterKeysDismissed() },
                onApplyFilters = { updatedFilters ->
                    viewModel.onApplyFiltersClicked(updatedFilters)
                },
                onClearFilters = {
                    viewModel.onClearFiltersInDialog()
                },
                onCancelButtonClick = {
                    viewModel.onFilterDialogCancelled()
                },
                snackbarHostState = snackbarHostState,
                scope = scope
            )
        }

        if (uiState.showAddKeyDialog) {
            AddKeyDialog(
                dialogState = addKeyDialogState,
                onStateChange = { newState -> viewModel.onAddKeyStateChanged(newState) },
                onDismissRequest = { viewModel.onAddKeyDialogDismissed() },
                onConfirmAddKey = { viewModel.onConfirmAddKey() },
                isAddingKey = isAddingKey
            )
        }
    }
}

@Composable
private fun KeysList(
    keys: List<Key>,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onItemClick: (keyId: String) -> Unit,
    isSelectionModeActive: Boolean,
    selectedItemIds: Set<String>,
    containerHeight: Dp,
    error: String?,
    isLoadingInitial: Boolean,
    modifier: Modifier = Modifier,
) {
    if (keys.isEmpty() && !isLoadingMore && !canLoadMore && !isLoadingInitial) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (error != null) "${stringResource(Res.string.error_loading_keys_colon)}\n$error"
                else stringResource(Res.string.keys_not_found),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(15.dp)
            )
        }
        return
    }

    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = keys,
                key = { _, key -> key.id }
            ) { index, key ->
                KeyItem(
                    keyData = key,
                    isSelected = selectedItemIds.contains(key.id),
                    isSelectionModeActive = isSelectionModeActive,
                    onItemClick = { onItemClick(key.id) }
                )

                if (keys.lastIndex != index) {
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                }

                if (index == keys.lastIndex - 5 && canLoadMore && !isLoadingMore) {
                    LaunchedEffect(key1 = canLoadMore, key2 = isLoadingMore) {
                        onLoadMore()
                    }
                }
            }

            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        CustomVerticalScrollbarForLazyList(
            lazyListState = listState,
            containerHeight = containerHeight,
            totalItemsCount = keys.size + if (isLoadingMore) 1 else 0,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun KeyItem(
    keyData: Key,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseBackgroundColor = when {
        keyData.isExpired || keyData.isBlocked -> DarkErrorContainer
        keyData.isGuestKey -> LightOnTertiaryContainer
        keyData.isSystemKey -> DarkOnTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val dynamicBackgroundColor = if (isSelectionModeActive && isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else baseBackgroundColor

    val contentColorForKey = if (isSelectionModeActive && isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else
        when {
            keyData.isExpired || keyData.isBlocked -> LightOnPrimary
            keyData.isGuestKey -> LightOnPrimary
            keyData.isSystemKey -> LightOnPrimary
            else -> MaterialTheme.colorScheme.onSurface
        }

    SelectionContainer {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = dynamicBackgroundColor,
                    shape = RoundedCornerShape(7.dp)
                )
                .padding(vertical = 8.dp, horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KeyDetailRow(
                    stringResource(Res.string.key_uid_colon),
                    keyData.id,
                    contentColorForKey
                )
                KeyDetailRow(
                    stringResource(Res.string.status), keyData.statusDisplay, contentColorForKey
                )
                KeyDetailRow(
                    stringResource(Res.string.owner),
                    keyData.userFullName ?: stringResource(Res.string.not_assigned),
                    contentColorForKey
                )
                KeyDetailRow(
                    stringResource(Res.string.valid_until),
                    keyData.expirationDisplay,
                    contentColorForKey
                )
                if (keyData.isGuestKey) {
                    KeyDetailRow(
                        stringResource(Res.string.key_type_colon),
                        stringResource(Res.string.guest_key),
                        contentColorForKey
                    )
                } else if (keyData.isSystemKey) {
                    KeyDetailRow(
                        stringResource(Res.string.key_type_colon),
                        stringResource(Res.string.system_key),
                        contentColorForKey
                    )
                } else KeyDetailRow(
                    stringResource(Res.string.key_type_colon),
                    stringResource(Res.string.regular_key),
                    contentColorForKey
                )
                keyData.comment?.takeIf { it.isNotBlank() }?.let {
                    KeyDetailRow(stringResource(Res.string.comment_colon), it, contentColorForKey)
                }
            }

            if (isSelectionModeActive) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onItemClick() },
                    modifier = Modifier.padding(start = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyDetailRow(
    label: String,
    value: String?,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    if (value.isNullOrBlank() && label != stringResource(Res.string.comment_colon)) {
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            maxLines = 1,
            color = valueColor,
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value ?: "",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.width(4.dp))
}