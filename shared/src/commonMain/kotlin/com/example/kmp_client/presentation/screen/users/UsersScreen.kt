package com.example.kmp_client.presentation.screen.users

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.user.User
import com.example.kmp_client.presentation.screen.users.UserScreenUiEvent.ShowSnackbar
import com.example.kmp_client.presentation.ui.component.CustomVerticalScrollbarForLazyList
import com.example.kmp_client.presentation.ui.component.ScreenBottomAppBar
import kotlinx.coroutines.launch
import moe.tlaster.precompose.koin.koinViewModel
import com.example.kmp_client.presentation.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    snackbarHostState: SnackbarHostState,
    onUserClick: (userId: Long) -> Unit
) {
    val viewModel: UsersViewModel = koinViewModel(UsersViewModel::class)
    val uiState by viewModel.uiState.collectAsState()
    val currentFilters by viewModel.userFilterModels.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val sharedViewModel: SharedAppViewModel = koinViewModel(SharedAppViewModel::class)
    val userDeletedEvent by sharedViewModel.userDeletedEvent.collectAsState()
    val userUpdatedEvent by sharedViewModel.userUpdatedEvent.collectAsState()
    val discoveredFeatureKeys by viewModel.discoveredFeatureKeys.collectAsState()
    val successMessage = stringResource(Res.string.success_user_deleted)

    LaunchedEffect(userDeletedEvent) {
        userDeletedEvent?.let { event ->
            if (event.success) {
                snackbarHostState.showSnackbar(successMessage)
                viewModel.refreshUsersList()
            }
            sharedViewModel.clearUserDeletedEvent()
        }
    }

    LaunchedEffect(userUpdatedEvent) {
        userUpdatedEvent?.let { event ->
            if (event.success) {
                viewModel.refreshUsersList()
            }
            sharedViewModel.clearUserUpdatedEvent()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            ScreenBottomAppBar(
                onFilterClick = {
                    if (!uiState.isSelectionModeActive) viewModel.onFilterButtonClicked()
                },
                areFiltersActive = uiState.areFiltersActive,
                onAddItemClick = {
                    viewModel.onAddUserClicked()
                },
                onRefreshClick = {
                    viewModel.loadInitialUsers()
                },
                isRefreshing = uiState.isLoading || uiState.isLoadingDictionaries || uiState.isLoadingMore,
                isSelectionModeActive = uiState.isSelectionModeActive,
                selectedItemsCount = uiState.selectedUserIds.size,
                onDeleteModeToggleClick = { viewModel.onDeleteButtonClicked() },
                onCancelSelectionClick = { viewModel.onCancelInSelectionModeClicked() },
                onApplySelectionClick = { viewModel.onApplyInSelectionModeClicked() },
                isDeletionInProgress = uiState.deletionInProgress
            )
        }
    ) { paddingValues ->
        LaunchedEffect(uiState.error, uiState.successMessage, uiState.selectionActionError) {
            when {
                uiState.error != null -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(uiState.error!!)
                        viewModel.clearError()
                    }
                }

                uiState.successMessage != null -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(uiState.successMessage!!)
                        viewModel.clearSuccessMessage()
                    }
                }

                uiState.selectionActionError != null -> {
                    uiState.selectionActionError?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearSelectionActionError()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is ShowSnackbar -> {
                        scope.launch {
                            snackbarHostState.showSnackbar(event.message, duration = event.duration)
                        }
                    }

                    is UserScreenUiEvent.ShowErrorDialog -> {
                        val combineErrorMessage =
                            "${event.title}:\n${event.errors.joinToString("\n")}"
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = combineErrorMessage,
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showConfirmDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeletionDialog() },
                title = { Text(stringResource(Res.string.delete_confirmation)) },
                text = { Text("Подтвердите удаление ${uiState.usersToDeleteCount} пользователей") },
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

            if ((uiState.isLoading || uiState.isLoadingDictionaries) && uiState.users.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                UserList(
                    uiState = uiState,
                    onLoadMore = { viewModel.loadMoreUsers() },
                    onItemClick = { userId ->
                        if (uiState.isSelectionModeActive)
                            viewModel.toggleUserSelection(userId)
                        else
                            onUserClick(userId)
                    },
                    containerHeight = containerHeight,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (uiState.showFilterDialog) {
        FilterDialog(
            userFilterModels = currentFilters,
            jobTitlesMap = uiState.jobTitlesMap,
            departmentsMap = uiState.departmentsMap,
            onDismissRequest = { viewModel.onFilterDialogCancelledAndDismissed() },
            onApplyFilters = { updatedFilters ->
                viewModel.onApplyFiltersClicked(updatedFilters)
            },
            onClearFilters = {
                viewModel.onClearFiltersInDialogRequested()
            },
            onCancelButtonClick = {
                viewModel.onFilterDialogCancelledAndDismissed()
            },
            snackbarHostState = snackbarHostState,
            scope = scope
        )
    }

    if (uiState.showAddUserDialog) {
        AddUserDialog(
            jobTitlesMap = uiState.jobTitlesMap,
            departmentsMap = uiState.departmentsMap,
            discoveredFeatureKeys = discoveredFeatureKeys,
            onDismissRequest = { viewModel.onAddUserDialogDismissed() },
            onConfirmAddUser = { userData -> viewModel.addUser(userData) },
            isAddingUser = uiState.isAddingUser,
            addUserError = uiState.addUserError
        )
    }
}

@Composable
private fun UserList(
    uiState: UsersUiState,
    onLoadMore: () -> Unit,
    onItemClick: (userId: Long) -> Unit,
    containerHeight: Dp,
    modifier: Modifier = Modifier
) {
    val users = uiState.users
    val isLoadingMore = uiState.isLoadingMore
    val canLoadMore = uiState.canLoadMore
    val jobTitlesMap = uiState.jobTitlesMap
    val departmentsMap = uiState.departmentsMap
    val isSelectionModeActive = uiState.isSelectionModeActive
    val selectedUserIds = uiState.selectedUserIds

    if (users.isEmpty() && !isLoadingMore && !canLoadMore && !uiState.isLoading && !uiState.isLoadingDictionaries) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (uiState.dictionariesError != null) stringResource(Res.string.error_loading_dictionaries)
                else if (uiState.error != null) stringResource(Res.string.error_loading_users)
                else stringResource(Res.string.users_not_found),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val listState = rememberLazyListState()

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = users,
                key = { _, user -> user.id }
            ) { index, user ->
                val jobTitleName = jobTitlesMap[user.jobTitleId]
                val departmentName = departmentsMap[user.departmentId]

                UserItem(
                    user = user,
                    jobTitleName = jobTitleName,
                    departmentName = departmentName,
                    isSelectionModeActive = isSelectionModeActive,
                    isSelected = selectedUserIds.contains(user.id),
                    onItemClick = { onItemClick(user.id) }
                )

                if (users.lastIndex != index) {
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                }

                if (index == users.lastIndex - 5 && canLoadMore && !isLoadingMore) {
                    LaunchedEffect(Unit) {
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
            totalItemsCount = users.size + if (isLoadingMore) 1 else 0,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun UserItem(
    user: User,
    jobTitleName: String?,
    departmentName: String?,
    isSelectionModeActive: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseBackgroundColor = if (user.guest) {
        LightOnTertiaryContainer
    } else MaterialTheme.colorScheme.surfaceVariant

    val dynamicBackgroundColor = if (isSelectionModeActive && isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        baseBackgroundColor
    }
    val contentColor = if (isSelectionModeActive && isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else if (user.guest) {
        LightOnPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = dynamicBackgroundColor,
                shape = RoundedCornerShape(7.dp)
            )
            .clickable(onClick = onItemClick)
            .padding(vertical = 8.dp, horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            UserDetailRow(stringResource(Res.string.employee_number), user.code, contentColor)
            UserDetailRow(stringResource(Res.string.surname), user.surname, contentColor)
            UserDetailRow(stringResource(Res.string.name), user.name, contentColor)
            UserDetailRow(stringResource(Res.string.patronymic), user.patronymic, contentColor)
            val displayJobTitle = when {
                jobTitleName != null -> jobTitleName
                user.jobTitleId != null && user.jobTitleId != 0L -> "ID: ${user.jobTitleId}"
                else -> ""
            }
            UserDetailRow(stringResource(Res.string.position), displayJobTitle, contentColor)
            val displayDepartment = when {
                departmentName != null -> departmentName
                user.departmentId != null && user.departmentId != 0L -> "ID: ${user.departmentId}"
                else -> stringResource(Res.string.not_specified)
            }
            UserDetailRow(stringResource(Res.string.department), displayDepartment, contentColor)
        }

        if (isSelectionModeActive) {
            androidx.compose.material3.Checkbox(
                checked = isSelected,
                onCheckedChange = { onItemClick() },
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun UserDetailRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}