package com.example.kmp_client.presentation.screen.key

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kmp_client.domain.util.DateTimeFormatters
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.kmp_client.domain.model.key.AddKeyDialogState
import com.example.kmp_client.domain.model.key.ReentryAllowedState
import com.example.kmp_client.domain.util.Validators
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.math.pow
import kmp_eraclient.shared.generated.resources.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKeyDialog(
    dialogState: AddKeyDialogState,
    onStateChange: (AddKeyDialogState) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmAddKey: () -> Unit,
    isAddingKey: Boolean
) {
    val extCodeOptions = remember { (1..16).toList() }
    var extCodeDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var displayExpirationDate by remember(dialogState.expiration.value) {
        mutableStateOf(
            TextFieldValue(
                text = try {
                    if (dialogState.expiration.value.isNotBlank()) {
                        val localDate = LocalDate.parse(dialogState.expiration.value)
                        DateTimeFormatters.ddMMyyFormatter.format(localDate)
                    } else ""
                } catch (_: Exception) {
                    if (dialogState.expiration.value.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
                        dialogState.expiration.value
                    } else ""
                },
                selection = TextRange(
                    try {
                        if (dialogState.expiration.value.isNotBlank()) {
                            val localDate = LocalDate.parse(dialogState.expiration.value)
                            DateTimeFormatters.ddMMyyFormatter.format(localDate).length
                        } else 0
                    } catch (_: Exception) {
                        if (dialogState.expiration.value.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
                            dialogState.expiration.value.length
                        } else 0
                    }
                )
            )
        )
    }
    val performFinalValidation: () -> Boolean = {
        var updatedState = dialogState
        var isValid = true

        if (updatedState.uid.value.isBlank()) {
            updatedState =
                updatedState.copy(uid = updatedState.uid.copy(errorMessage = "UID обязателен для заполнения"))
            isValid = false
        } else {
            if (updatedState.uid.errorMessage == "UID обязателен для заполнения") {
                updatedState = updatedState.copy(uid = updatedState.uid.copy(errorMessage = null))
            }
        }

        if (updatedState.expiration.value.isNotBlank()) {
            try {
                LocalDate.parse(updatedState.expiration.value)
                if (updatedState.expiration.errorMessage == "Неверный формат даты" || updatedState.expiration.errorMessage == "Формат ДД.ММ.ГГГГ") {
                    updatedState =
                        updatedState.copy(expiration = updatedState.expiration.copy(errorMessage = null))
                }
            } catch (_: Exception) {
                updatedState =
                    updatedState.copy(expiration = updatedState.expiration.copy(errorMessage = "Неверный формат даты"))
                isValid = false
            }
        } else {
            if (updatedState.expiration.errorMessage != null) {
                updatedState =
                    updatedState.copy(expiration = updatedState.expiration.copy(errorMessage = null))
            }
        }

        val descriptionError = Validators.maxLength(20)(updatedState.description.value)
        if (descriptionError != null) {
            updatedState =
                updatedState.copy(description = updatedState.description.copy(errorMessage = descriptionError))
        } else {
            if (updatedState.description.errorMessage != null && updatedState.description.errorMessage == Validators.maxLength(
                    20
                )("")
            ) {
                updatedState =
                    updatedState.copy(description = updatedState.description.copy(errorMessage = null))
            }
        }

        if (!isValid) {
            updatedState = updatedState.copy(overallErrorMessage = "Ошибки заполнения форм")
        } else {
            if (updatedState.overallErrorMessage == "Исправьте ошибки заполнения форм") {
                updatedState = updatedState.copy(overallErrorMessage = null)
            }
        }
        onStateChange(updatedState)
        isValid
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    stringResource(Res.string.add_key_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 15.dp)
                )

                if (dialogState.overallErrorMessage != null) {
                    Text(
                        dialogState.overallErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dialogState.uid.value,
                        onValueChange = { newValue ->
                            val sanitized = newValue.filter { it.isDigit() }.take(19)
                            var error: String? =
                                dialogState.uid.errorMessage.takeUnless { it == "UID обязателен для заполнения" }

                            onStateChange(
                                dialogState.copy(
                                    uid = dialogState.uid.copy(
                                        value = sanitized,
                                        errorMessage = error
                                    )
                                )
                            )
                        },
                        label = { Text(stringResource(Res.string.key_uid)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = dialogState.uid.errorMessage != null,
                        supportingText = dialogState.uid.errorMessage?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    OutlinedTextField(
                        value = displayExpirationDate,
                        onValueChange = { newDisplayValue ->
                            val digitsOnly = newDisplayValue.text.filter { it.isDigit() }.take(8)

                            val formattedValue = when {
                                digitsOnly.length <= 2 -> digitsOnly
                                digitsOnly.length <= 4 -> "${digitsOnly.take(2)}.${digitsOnly.drop(2)}"
                                digitsOnly.length <= 8 -> "${digitsOnly.take(2)}.${digitsOnly.slice(2..3)}.${digitsOnly.drop(4)}"
                                else -> digitsOnly.take(8).let { "${it.take(2)}.${it.slice(2..3)}.${it.drop(4)}" }
                            }

                            val newCursorPosition = formattedValue.length

                            displayExpirationDate = TextFieldValue(
                                text = formattedValue,
                                selection = TextRange(newCursorPosition)
                            )

                            var newIsoValue = ""
                            var error: String? = null
                            if (formattedValue.isNotBlank()) {
                                try {
                                    val parsedDate = DateTimeFormatters.ddMMyyFormatter.parse(
                                        formattedValue
                                    )
                                    newIsoValue = parsedDate.toString()
                                    error = null
                                } catch (_: Exception) {
                                    newIsoValue = ""
                                    error = "Неверный формат даты"
                                }
                            } else error = null

                            val finalErrorMessage = if (error == null && dialogState.expiration.errorMessage != null && dialogState.expiration.errorMessage != "Неверный формат даты для отправки") {
                                null
                            } else error ?: dialogState.expiration.errorMessage
                            onStateChange(
                                dialogState.copy(
                                    expiration = dialogState.expiration.copy(
                                        value = newIsoValue,
                                        errorMessage = finalErrorMessage
                                    )
                                )
                            )
                        },
                        label = { Text(stringResource(Res.string.expiration_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = "Выбрать дату",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        isError = dialogState.expiration.errorMessage != null,
                        supportingText = dialogState.expiration.errorMessage?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )
                    if (showDatePicker) {
                        val currentSystemDate =
                            Clock.System.todayIn(TimeZone.currentSystemDefault())
                        val yearRange = (currentSystemDate.year - 10)..(currentSystemDate.year + 20)
                        val initialDateMillis =
                            dialogState.expiration.value.takeIf { it.isNotBlank() }
                                ?.let { dateString ->
                                    try {
                                        val localDate = LocalDate.parse(dateString)
                                        if (localDate.year in yearRange) {
                                            localDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                                        } else {
                                            null
                                        }
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = initialDateMillis,
                            yearRange = (currentSystemDate.year - 10)..(currentSystemDate.year + 20)
                        )

                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDatePicker = false
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                                            .toLocalDateTime(TimeZone.UTC).date
                                        onStateChange(
                                            dialogState.copy(
                                                expiration = dialogState.expiration.copy(
                                                    value = selectedDate.toString(),
                                                    errorMessage = null
                                                )
                                            )
                                        )
                                    }
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDatePicker = false
                                }) { Text(stringResource(Res.string.cancel)) }
                            }
                        ) { DatePicker(state = datePickerState) }
                    }

                    var reentryDropDownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = reentryDropDownExpanded,
                        onExpandedChange = {
                            reentryDropDownExpanded = !reentryDropDownExpanded
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = when (dialogState.reentryAlwaysAllowed.value) {
                                true -> stringResource(Res.string.yes)
                                false -> stringResource(Res.string.no)
                                null -> ""
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.always_allow_reentry)) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reentryDropDownExpanded) },
                            isError = dialogState.reentryAlwaysAllowed.errorMessage != null,
                            supportingText = dialogState.reentryAlwaysAllowed.errorMessage?.let {
                                { Text(it, color = MaterialTheme.colorScheme.error) }
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = reentryDropDownExpanded,
                            onDismissRequest = { reentryDropDownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.yes)) },
                                onClick = {
                                    onStateChange(
                                        dialogState.copy(
                                            reentryAlwaysAllowed = ReentryAllowedState(
                                                value = true,
                                                errorMessage = null
                                            )
                                        )
                                    )
                                    reentryDropDownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.no)) },
                                onClick = {
                                    onStateChange(
                                        dialogState.copy(
                                            reentryAlwaysAllowed = ReentryAllowedState(
                                                value = false
                                            )
                                        )
                                    )
                                    reentryDropDownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.not_selected)) },
                                onClick = {
                                    onStateChange(
                                        dialogState.copy(
                                            reentryAlwaysAllowed = ReentryAllowedState(
                                                value = null
                                            )
                                        )
                                    )
                                    reentryDropDownExpanded = false
                                }
                            )
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = extCodeDropdownExpanded,
                        onExpandedChange = { extCodeDropdownExpanded = !extCodeDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (dialogState.extCode.selectionOptions.isEmpty()) stringResource(Res.string.not_selected)
                            else dialogState.extCode.selectionOptions.sorted().joinToString(", "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.ext_code)) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = extCodeDropdownExpanded) },
                            isError = dialogState.extCode.errorMessage != null,
                            supportingText = dialogState.extCode.errorMessage?.let {
                                { Text(it, color = MaterialTheme.colorScheme.error) }
                            }
                        )
                        DropdownMenu(
                            expanded = extCodeDropdownExpanded,
                            onDismissRequest = { extCodeDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            extCodeOptions.forEach { option ->
                                val isSelected =
                                    dialogState.extCode.selectionOptions.contains(option)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isSelected, onCheckedChange = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "EXT $option"
                                            )
                                        }
                                    },
                                    onClick = {
                                        val currentSelected =
                                            dialogState.extCode.selectionOptions.toMutableSet()
                                        if (isSelected) {
                                            currentSelected.remove(option)
                                        } else {
                                            currentSelected.add(option)
                                        }
                                        val calculated =
                                            currentSelected.sumOf { 2.0.pow(it - 1).toInt() }
                                        onStateChange(
                                            dialogState.copy(
                                                extCode = dialogState.extCode.copy(
                                                    selectionOptions = currentSelected,
                                                    calculatedValue = calculated
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dialogState.description.value,
                        onValueChange = { newValue ->
                            val sanitized = newValue.take(20)
                            onStateChange(
                                dialogState.copy(
                                    description = dialogState.description.copy(
                                        value = sanitized,
                                        errorMessage = Validators.maxLength(20)(sanitized)
                                    )
                                )
                            )
                        },
                        label = { Text(stringResource(Res.string.comment)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = dialogState.description.errorMessage != null,
                        supportingText = dialogState.description.errorMessage?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        enabled = !isAddingKey
                    ) { Text(stringResource(Res.string.cancel)) }
                    Button(
                        onClick = { if (performFinalValidation()) onConfirmAddKey() },
                        enabled = !isAddingKey
                    ) {
                        if (isAddingKey) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else Text(stringResource(Res.string.add))
                    }
                }
            }
        }
    }
}