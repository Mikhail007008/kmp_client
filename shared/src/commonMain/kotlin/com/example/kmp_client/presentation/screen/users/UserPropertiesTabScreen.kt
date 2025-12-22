package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kmp_client.presentation.ui.component.getDynamicTextFieldColors
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun UserPropertiesTabScreen(
    uiState: UserDetailUiState,
    viewModel: UserDetailViewModel,
    jobTitlesMap: Map<Long, String>,
    departmentsMap: Map<Long, String>,
    modifier: Modifier = Modifier
) {
    val user = uiState.user ?: return

    Column(modifier = modifier.fillMaxSize()) {
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(15.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = viewModel::onCodeChanged,
                    label = { Text(stringResource(Res.string.employee_number)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = getDynamicTextFieldColors(uiState.code.isNotBlank())
                )
                OutlinedTextField(
                    value = uiState.surname,
                    onValueChange = viewModel::onSurnameChanged,
                    label = { Text(stringResource(Res.string.surname)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = getDynamicTextFieldColors(uiState.surname.isNotBlank())
                )
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text(stringResource(Res.string.name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = getDynamicTextFieldColors(uiState.name.isNotBlank())
                )
                OutlinedTextField(
                    value = uiState.patronymic,
                    onValueChange = viewModel::onPatronymicChanged,
                    label = { Text(stringResource(Res.string.patronymic)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = getDynamicTextFieldColors(uiState.patronymic.isNotBlank())
                )
                DropdownMenuForEntity(
                    label = stringResource(Res.string.job_title),
                    optionsMap = jobTitlesMap,
                    selectedId = uiState.selectedJobTitleId,
                    onSelectionChanged = viewModel::onSelectedJobTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && jobTitlesMap.isNotEmpty()
                )
                DropdownMenuForEntity(
                    label = stringResource(Res.string.department),
                    optionsMap = departmentsMap,
                    selectedId = uiState.selectedDepartmentId,
                    onSelectionChanged = viewModel::onSelectedDepartmentChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && departmentsMap.isNotEmpty()
                )
                uiState.discoveredFeatureKeysOnDetail.forEach { featureKey ->
                    val displayName = "${stringResource(Res.string.property)} ${featureKey.removePrefix("feature")}"
                    val currentValue = uiState.editableFeatures[featureKey] ?: ""
                    OutlinedTextField(
                        value = uiState.editableFeatures[featureKey] ?: "",
                        onValueChange = { newValue -> viewModel.onFeatureValueChanged(featureKey, newValue)},
                        label = { Text(displayName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        colors = getDynamicTextFieldColors(hasSelection = currentValue.isNotBlank())
                    )
                }
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChanged,
                    label = { Text(stringResource(Res.string.phone)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = getDynamicTextFieldColors(uiState.phone.isNotBlank())
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text(stringResource(Res.string.email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = getDynamicTextFieldColors(uiState.email.isNotBlank())
                )
                OutlinedTextField(
                    value = uiState.balance,
                    onValueChange = viewModel::onBalanceChanged,
                    label = { Text(stringResource(Res.string.balance)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = getDynamicTextFieldColors(uiState.balance.isNotBlank())
                )
                HorizontalDivider()

                UserDetailRow(stringResource(Res.string.guest_colon), if (user.guest) stringResource(Res.string.yes) else stringResource(Res.string.no))
                UserDetailRow(stringResource(Res.string.creation_time_colon), value = uiState.creationTimeDisplay)

                Spacer(Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onCancelChangedClicked() },
                        enabled = uiState.hasChanges && !uiState.isLoading
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(
                        onClick = { viewModel.onDeleteUserClicked() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = !uiState.isLoading && !uiState.deleteInProgress
                    ) {
                        Text(stringResource(Res.string.delete))
                    }
                    Button(
                        onClick = { viewModel.onApplyChangesClicked() },
                        enabled = uiState.hasChanges && !uiState.isLoading
                    ) {
                        Text(stringResource(Res.string.apply))
                    }
                }
            }
        }

        if (uiState.showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeleteDialog() },
                title = { Text(stringResource(Res.string.delete_user)) },
                text = {
                    Text("${stringResource(Res.string.delete_user_confirm)} ${uiState.user.name} ${uiState.user.surname}?")
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.onConfirmDeleteUser() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(Res.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun UserDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuForEntity(
    label: String,
    optionsMap: Map<Long, String>,
    selectedId: Long?,
    onSelectionChanged: (selectedId: Long?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowClear: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOptionName = selectedId?.let { optionsMap[it] } ?: ""
    val currentTextFieldColors = if (selectedId != null) {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    } else {
        OutlinedTextFieldDefaults.colors()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOptionName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            enabled = enabled,
            colors = currentTextFieldColors
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
            optionsMap.entries.forEach { (id, name) ->
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