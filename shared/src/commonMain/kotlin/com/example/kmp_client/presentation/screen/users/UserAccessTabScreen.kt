package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.useraccess.UserAccessKeyDetail
import com.example.kmp_client.domain.model.useraccess.UserAccessSchemeDetail
import kmp_eraclient.shared.generated.resources.Res
import kmp_eraclient.shared.generated.resources.access_schemes
import kmp_eraclient.shared.generated.resources.add_key
import kmp_eraclient.shared.generated.resources.always
import kmp_eraclient.shared.generated.resources.control_key
import kmp_eraclient.shared.generated.resources.key
import kmp_eraclient.shared.generated.resources.no_keys
import kmp_eraclient.shared.generated.resources.no_schemes
import kmp_eraclient.shared.generated.resources.not_assigned
import kmp_eraclient.shared.generated.resources.refresh
import kmp_eraclient.shared.generated.resources.retry
import kmp_eraclient.shared.generated.resources.schedule_current
import kmp_eraclient.shared.generated.resources.schedule_next
import kmp_eraclient.shared.generated.resources.success_user_deleted
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserAccessTabScreen(
    uiState: UserDetailUiState,
    viewModel: UserDetailViewModel,
    userId: Long,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        when {
            uiState.isAccessTabLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.accessTabError != null && uiState.userAccessDetails == null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        stringResource(Res.string.failed_to_load_access),
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = { viewModel.loadUserAccessDetails(forceRefresh = true) }) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }

            uiState.userAccessDetails != null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (uiState.userAccessDetails.keys.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Text(
                                stringResource(Res.string.no_keys),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            items(uiState.userAccessDetails.keys, key = { it.key }) { keyDetail ->
                                UserAccessKeyItem(
                                    keyDetail = keyDetail,
                                    onClick = { viewModel.onEditAccessKeyClicked(keyDetail) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onAddAccessKeyClicked() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.add_key))
                        }
                        OutlinedButton(
                            onClick = { viewModel.loadUserAccessDetails(forceRefresh = true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.refresh))
                        }
                    }
                }
            }
        }

        if (uiState.showEditAccessDialog && uiState.editingAccessKey != null) {
            EditUserAccessDialog(
                originalKeyDetail = uiState.editingAccessKey,
                allSchedules = uiState.allSchedules,
                allAccessSchemes = uiState.allAccessSchemes,
                onDismiss = viewModel::onDismissEditAccessDialog,
                onApply = { updatedKeyDetail ->
                    viewModel.onApplyAccessChanges(updatedKeyDetail)
                },
                onDeleteKeyConfirmed = {
                    viewModel.onDeleteAccessKey(uiState.editingAccessKey.key)
                },
                isLoadingDictionaries = uiState.isLoadingAccessDictionaries,
                dictionariesError = uiState.accessDictionariesError,
                dialogError = uiState.editAccessDialogError,
                onClearDialogError = viewModel::clearEditAccessDialogError
            )
        }

        if (uiState.showAssignKeyDialog) {
            AssignKeyDialog(
                availableKeys = uiState.availableKeysForAssignment,
                selectedKeyId = uiState.selectedKeyForAssignmentId,
                onKeySelected = viewModel::onSelectKeyForAssignment,
                onConfirm = viewModel::onConfirmAssignKey,
                onDismiss = viewModel::onDismissAssignKeyDialog,
                isLoading = uiState.isLoadingAvailableKeys || uiState.isLoadingMoreAssignKeys,
                error = uiState.assignKeyDialogError,
                canLoadMore = uiState.canLoadMoreAssignKeys,
                onLoadMore = { viewModel.loadAvailableKeysForAssignment(isInitialLoad = false) }
            )
        }
    }
}

@Composable
fun UserAccessKeyItem(
    keyDetail: UserAccessKeyDetail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text("${stringResource(Res.string.key_colon)} ${keyDetail.key}", style = MaterialTheme.typography.titleMedium)
            keyDetail.controlKey?.let {
                if (it.isNotBlank()) {
                    Text("${stringResource(Res.string.control_key)} $it", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(7.dp))

            if (keyDetail.accessSchemes.isNotEmpty()) {
                Text(
                    stringResource(Res.string.access_schemes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(7.dp))
                keyDetail.accessSchemes.forEachIndexed { index, schemeDetail ->
                    UserAccessSchemeItem(schemeDetail)
                    if (index < keyDetail.accessSchemes.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            } else Text(stringResource(Res.string.no_schemes))
        }
    }
}

@Composable
fun UserAccessSchemeItem(schemeDetail: UserAccessSchemeDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = schemeDetail.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.schedule_current), style = MaterialTheme.typography.bodyMedium)
            if (schemeDetail.anytime) {
                Text(
                    stringResource(Res.string.always),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    schemeDetail.currentYearScheduleName ?: stringResource(Res.string.not_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        schemeDetail.nextYearScheduleName?.let { nextYearSchedule ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.schedule_next), style = MaterialTheme.typography.bodyMedium)
                Text(
                    nextYearSchedule,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}