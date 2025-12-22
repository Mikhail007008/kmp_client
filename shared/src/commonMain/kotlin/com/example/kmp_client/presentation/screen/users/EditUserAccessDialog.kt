package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.example.kmp_client.data.remote.dto.mapper.toDomain
import com.example.kmp_client.data.remote.dto.response.AccessSchemeDTO
import com.example.kmp_client.data.remote.dto.response.ScheduleDto
import com.example.kmp_client.domain.model.useraccess.UserAccessKeyDetail
import com.example.kmp_client.domain.model.useraccess.UserAccessSchemeDetail
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun EditUserAccessDialog(
    originalKeyDetail: UserAccessKeyDetail,
    allSchedules: List<ScheduleDto>,
    allAccessSchemes: List<AccessSchemeDTO>,
    onDismiss: () -> Unit,
    onApply: (UserAccessKeyDetail) -> Unit,
    onDeleteKeyConfirmed: () -> Unit,
    isLoadingDictionaries: Boolean,
    dictionariesError: String?,
    dialogError: String?,
    onClearDialogError: () -> Unit
) {
    val initialEditableSchemes = remember(originalKeyDetail.accessSchemes, allSchedules, allAccessSchemes) {
        originalKeyDetail.accessSchemes.map { domainScheme ->
            EditableSchemeUiState(
                uniqueId = domainScheme.id.toString(),
                selectedSchemeId = domainScheme.id,
                schemeName = domainScheme.name,
                selectedCurrentScheduleId = domainScheme.currentYearScheduleId,
                currentScheduleName = domainScheme.currentYearScheduleName,
                selectedNextScheduleId = domainScheme.nextYearScheduleId,
                nextScheduleName = domainScheme.nextYearScheduleName,
                isAnytime = domainScheme.anytime,
                isNewlyAdded = false
            )
        }
    }

    val originalDomainSchemes = remember(originalKeyDetail) {
        originalKeyDetail.accessSchemes
    }

    var editableSchemes by remember {
        mutableStateOf(initialEditableSchemes.toMutableStateList())
    }

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var showConfirmDeleteKeyDialog by remember { mutableStateOf(false) }

    val hasChanges by remember(editableSchemes, originalDomainSchemes, allSchedules) {
        derivedStateOf {
            val currentDomainConverted = editableSchemes
                .mapNotNull { uiState -> uiState.toDomain(allSchedules) }
                .sortedBy { it.id }

            val originalSorted = originalDomainSchemes.sortedBy { it.id }

            if (currentDomainConverted.size != originalSorted.size) {
                return@derivedStateOf true
            }

            currentDomainConverted.zip(originalSorted).any { (current, original) ->
                current.id != original.id ||
                        current.name != original.name ||
                        current.anytime != original.anytime ||
                        current.currentYearScheduleId != original.currentYearScheduleId ||
                        current.currentYearScheduleName != original.currentYearScheduleName ||
                        current.nextYearScheduleId != original.nextYearScheduleId ||
                        current.nextYearScheduleName != original.nextYearScheduleName
            }
        }
    }

    if (showConfirmDeleteKeyDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteKeyDialog = false },
            title = { Text(stringResource(Res.string.delete_key))},
            text = { Text(stringResource(Res.string.confirm_delete_key))},
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteKeyDialog = false
                        onDeleteKeyConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteKeyDialog = false}) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            onClearDialogError()
            onDismiss
        },
        title = { Text(stringResource(Res.string.editing)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${stringResource(Res.string.key_colon)} ${originalKeyDetail.key}",
                    style = MaterialTheme.typography.titleMedium
                )
                originalKeyDetail.controlKey?.let {
                    if (it.isNotBlank()) Text(
                        "${stringResource(Res.string.control_key)} $it",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))

                if (isLoadingDictionaries) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (dictionariesError != null) {
                    Text(stringResource(Res.string.error_loading_dictionaries), color = MaterialTheme.colorScheme.error)
                } else if (allAccessSchemes.isEmpty() && allSchedules.isEmpty()) {
                    Text(stringResource(Res.string.schemes_schedules_not_load))
                } else {
                    if (!isLoadingDictionaries) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                editableSchemes,
                                key = { _, item -> item.uniqueId }
                            ) { index, schemeState ->
                                EditableAccessSchemeItem(
                                    editableSchemeState = schemeState,
                                    allSchemes = allAccessSchemes,
                                    allSchedules = allSchedules,
                                    onSchemeChanged = { updatedState ->
                                        val newList = editableSchemes.toMutableList()
                                        newList[index] = updatedState
                                        editableSchemes = newList.toMutableStateList()
                                    },
                                    onDelete = {
                                        val newList = editableSchemes.toMutableList()
                                        newList.removeAt(index)
                                        editableSchemes = newList.toMutableStateList()
                                    }
                                )
                                if (index < editableSchemes.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val newScheme = EditableSchemeUiState(
                                uniqueId = uuid4().toString(),
                                selectedSchemeId = null,
                                schemeName = null,
                                selectedCurrentScheduleId = null,
                                currentScheduleName = null,
                                selectedNextScheduleId = null,
                                nextScheduleName = null,
                                isAnytime = true,
                                isNewlyAdded = true
                            )
                            editableSchemes = (editableSchemes + newScheme).toMutableStateList()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingDictionaries
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить схему")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(Res.string.add_scheme))
                    }
                }

                if (dialogError != null) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = dialogError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 7.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedDomainSchemes = editableSchemes.mapNotNull { editable ->
                        if (editable.selectedSchemeId == null && editable.isNewlyAdded) return@mapNotNull null
                        if (editable.schemeName == null || editable.schemeName!!.isBlank()) return@mapNotNull null

                        UserAccessSchemeDetail(
                            id = editable.selectedSchemeId!!,
                            name = editable.schemeName!!,
                            anytime = editable.isAnytime,
                            currentYearScheduleId = if (editable.isAnytime) null else editable.selectedCurrentScheduleId,
                            currentYearScheduleName = if (editable.isAnytime) null else editable.selectedCurrentScheduleId?.let { sId ->
                                allSchedules.find { it.id == sId }?.let { "[${it.year}] ${it.name}" }
                            },
                            nextYearScheduleId = if (editable.isAnytime || editable.selectedCurrentScheduleId == null) null else editable.selectedNextScheduleId,
                            nextYearScheduleName = if (editable.isAnytime || editable.selectedCurrentScheduleId == null) null else editable.selectedNextScheduleId?.let { sId ->
                                allSchedules.find { it.id == sId }?.let { "[${it.year}] ${it.name}" }
                            }
                        )
                    }
                    val updatedAccessKey =
                        originalKeyDetail.copy(accessSchemes = updatedDomainSchemes)
                    onApply(updatedAccessKey)
                },
                enabled = !isLoadingDictionaries && dictionariesError == null
                        && editableSchemes.all { !it.isNewlyAdded || it.selectedSchemeId != null }
                        && hasChanges
            ) {
                Text(stringResource(Res.string.apply))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { showConfirmDeleteKeyDialog = true },
                    colors = textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.delete_this_key))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        }
    )

    LaunchedEffect(editableSchemes.size) {
        if (editableSchemes.isNotEmpty() && editableSchemes.last().isNewlyAdded) {
            if (lazyListState.layoutInfo.totalItemsCount == editableSchemes.size) {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(editableSchemes.lastIndex)
                }
            }
        }
    }
}