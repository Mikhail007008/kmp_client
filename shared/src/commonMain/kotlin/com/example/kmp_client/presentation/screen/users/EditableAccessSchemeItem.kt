package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmp_client.data.remote.dto.response.AccessSchemeDTO
import com.example.kmp_client.data.remote.dto.response.ScheduleDto
import com.example.kmp_client.presentation.ui.component.getDynamicTextFieldColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun EditableAccessSchemeItem(
    editableSchemeState: EditableSchemeUiState,
    allSchemes: List<AccessSchemeDTO>,
    allSchedules: List<ScheduleDto>,
    onSchemeChanged: (EditableSchemeUiState) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schemesNameMap = remember(allSchemes) { allSchemes.associateBy { it.id } }
    val scheduleFormattedNameMap = remember(allSchedules) {
        allSchedules.associateBy(
            { it.id },
            { "[${it.year}] ${it.name}" })
    }

    val currentYear = remember { getCurrentYear() }
    val schedulesForCurrentYear = remember(allSchedules, currentYear, scheduleFormattedNameMap) {
        allSchedules
            .filter { it.year == currentYear }
            .associate { it.id to (scheduleFormattedNameMap[it.id] ?: it.name) }
    }

    val schedulesForNextYear = remember(allSchedules, currentYear, scheduleFormattedNameMap) {
        allSchedules
            .filter { it.year == currentYear + 1 }
            .associate { it.id to (scheduleFormattedNameMap[it.id] ?: it.name) }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                if (editableSchemeState.isNewlyAdded) {
                    CustomDropdownMenu(
                        label = stringResource(Res.string.access_schemes),
                        options = allSchemes.associate { it.id to it.name },
                        selectedId = editableSchemeState.selectedSchemeId,
                        selectedNameDisplay = editableSchemeState.schemeName,
                        onSelectionChanged = { newSchemeId ->
                            val newSchemeName = newSchemeId?.let { schemesNameMap[it] }
                            val newActualSchemeName = newSchemeName?.name
                            onSchemeChanged(
                                editableSchemeState.copy(
                                    selectedSchemeId = newSchemeId,
                                    schemeName = newActualSchemeName,
                                    isAnytime = true,
                                    selectedCurrentScheduleId = null,
                                    currentScheduleName = null,
                                    selectedNextScheduleId = null,
                                    nextScheduleName = null
                                )
                            )
                        },
                        enabled = allSchemes.isNotEmpty(),
                        allowClear = false,
                        colors = editableSchemeState.selectedSchemeId != null
                    )
                } else {
                    OutlinedTextField(
                        value = editableSchemeState.schemeName ?: stringResource(Res.string.scheme_not_found),
                        onValueChange = {},
                        label = { Text(stringResource(Res.string.access_scheme)) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.primary,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete_scheme))
            }
        }
    }

    CustomDropdownMenu(
        label = stringResource(Res.string.schedule_current),
        options = schedulesForCurrentYear,
        selectedId = editableSchemeState.selectedCurrentScheduleId,
        selectedNameDisplay = if (editableSchemeState.isAnytime) "[****] всегда" else editableSchemeState.currentScheduleName,
        onSelectionChanged = { newCurrentScheduleId ->
            val newIsAnytime = newCurrentScheduleId == null
            val newCurrentScheduleName = if (newIsAnytime) {
                null
            } else {
                newCurrentScheduleId.let { scheduleFormattedNameMap[it] }
            }
            onSchemeChanged(
                editableSchemeState.copy(
                    selectedCurrentScheduleId = newCurrentScheduleId,
                    currentScheduleName = newCurrentScheduleName,
                    isAnytime = newIsAnytime,
                    selectedNextScheduleId = if (newIsAnytime) null else editableSchemeState.selectedNextScheduleId,
                    nextScheduleName = if (newIsAnytime) null else editableSchemeState.nextScheduleName
                )
            )
        },

        enabled = editableSchemeState.selectedSchemeId != null && schedulesForCurrentYear.isNotEmpty(),
        allowClear = true,
        colors = editableSchemeState.isAnytime && editableSchemeState.selectedNextScheduleId == null
    )

    val isNextYearScheduleActuallyEnabled =
        editableSchemeState.selectedSchemeId != null &&
                !editableSchemeState.isAnytime &&
                editableSchemeState.selectedCurrentScheduleId != null

    CustomDropdownMenu(
        label = stringResource(Res.string.schedule_next),
        options = schedulesForNextYear,
        selectedId = editableSchemeState.selectedNextScheduleId,
        selectedNameDisplay = editableSchemeState.nextScheduleName,
        onSelectionChanged = { newNextScheduleId ->
            val newNextScheduleName = if (editableSchemeState.isAnytime || editableSchemeState.selectedCurrentScheduleId == null) {
                null
            } else {
                newNextScheduleId?.let { scheduleFormattedNameMap[it] }
            }
            onSchemeChanged(
                editableSchemeState.copy(
                    selectedNextScheduleId = newNextScheduleId,
                    nextScheduleName = newNextScheduleName
                )
            )
        },
        enabled = isNextYearScheduleActuallyEnabled && allSchedules.isNotEmpty(),
        allowClear = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    label: String,
    options: Map<Long, String>,
    selectedId: Long?,
    selectedNameDisplay: String?,
    onSelectionChanged: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowClear: Boolean = true,
    colors: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelectionName = selectedNameDisplay ?: selectedId?.let { options[it] } ?: ""
    val actualColors = if (colors && selectedId == null) {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        getDynamicTextFieldColors(
            hasSelection = selectedId != null
        )
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = currentSelectionName,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            enabled = enabled,
            colors = actualColors
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (allowClear && selectedId != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.not_selected)) },
                    onClick = {
                        onSelectionChanged(null)
                        expanded = false
                    }
                )
                HorizontalDivider()
            }
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelectionChanged(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getCurrentYear(): Int {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
}