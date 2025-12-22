package com.example.kmp_client.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.filter.FieldFilterState
import com.example.kmp_client.domain.model.filter.FilterOperator
import com.example.kmp_client.domain.util.DateTimeFormatters
import com.example.kmp_client.domain.util.Validators
import kmp_eraclient.shared.generated.resources.Res
import kmp_eraclient.shared.generated.resources.cancel
import kmp_eraclient.shared.generated.resources.ok
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterableTextField(
    label: String,
    filterState: FieldFilterState,
    onStateChange: (FieldFilterState) -> Unit,
    activeColor: Color? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputSanitizer: (currentValue: String, newValue: String) -> String = { _, new -> new },
) {
    var expandedOperators by remember { mutableStateOf(false) }
    val currentActiveColor = if (filterState.isEnabled) activeColor else null
    val focusManager = LocalFocusManager.current

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = filterState.isEnabled,
            onCheckedChange = { isChecked ->
                if (!isChecked) {
                    focusManager.clearFocus()
                    onStateChange(
                        filterState.copy(
                            isEnabled = isChecked,
                            value = "",
                            errorMessage = null
                        )
                    )
                } else {
                    onStateChange(filterState.copy(isEnabled = isChecked))
                }
            }
        )

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = filterState.value,
                onValueChange = { newValue ->
                    val sanitizedValue = inputSanitizer(filterState.value, newValue)
                    onStateChange(filterState.copy(value = sanitizedValue))
                },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = filterState.isEnabled,
                singleLine = true,
                isError = filterState.errorMessage != null && filterState.value.isNotBlank(),
                supportingText = {
                    if (filterState.errorMessage != null && filterState.value.isNotBlank()) {
                        Text(
                            filterState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = if (currentActiveColor != null) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentActiveColor,
                        focusedLabelColor = currentActiveColor,
                        cursorColor = currentActiveColor,
                        unfocusedBorderColor = currentActiveColor
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                },
                keyboardOptions = keyboardOptions
            )
        }

        Box(modifier = Modifier.padding(start = 7.dp)) {
            OutlinedButton(
                onClick = { expandedOperators = true },
                enabled = filterState.isEnabled,
                modifier = Modifier.defaultMinSize(minWidth = 60.dp),
                border = if (currentActiveColor != null) BorderStroke(
                    1.dp,
                    currentActiveColor
                ) else null,
                colors = if (currentActiveColor != null) ButtonDefaults.outlinedButtonColors(
                    contentColor = currentActiveColor
                ) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(filterState.selectedOperator.symbol, maxLines = 1)
            }
            DropdownMenu(
                expanded = expandedOperators,
                onDismissRequest = { expandedOperators = false }
            ) {
                filterState.availableOperators.forEach { operator ->
                    DropdownMenuItem(
                        text = { Text("${operator.symbol} ${operator.displayName}") },
                        onClick = {
                            onStateChange(filterState.copy(selectedOperator = operator))
                            expandedOperators = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <K> FilterableDropdownField(
    label: String,
    filterState: FieldFilterState,
    optionsMap: Map<K, String>,
    keyToString: (K) -> String,
    stringToKey: (String) -> K?,
    onStateChange: (FieldFilterState) -> Unit,
    activeColor: Color? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedOperators by remember { mutableStateOf(false) }
    val currentActiveColor = if (filterState.isEnabled) activeColor else null
    val operatorButtonEnabled = filterState.isEnabled && filterState.availableOperators.size > 1

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = filterState.isEnabled,
            onCheckedChange = { isChecked -> onStateChange(filterState.copy(isEnabled = isChecked)) }
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (filterState.isEnabled) expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = filterState.value.takeIf { it.isNotBlank() }
                    ?.let { stringToKey(it)?.let { key -> optionsMap[key] } } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = filterState.isEnabled,
                colors = if (currentActiveColor != null) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentActiveColor,
                        unfocusedBorderColor = if (filterState.isEnabled) currentActiveColor else OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                        focusedLabelColor = currentActiveColor,
                        focusedTrailingIconColor = currentActiveColor
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val optionsEntries = optionsMap.entries.toList()
                optionsEntries.forEachIndexed { index, entry ->
                    val (itemKey, displayText) = entry
                    DropdownMenuItem(
                        text = { Text(displayText) },
                        onClick = {
                            val updatedFilterState = if (filterState.availableOperators.size == 1 &&
                                filterState.availableOperators.first() == FilterOperator.EQ
                            ) {
                                filterState.copy(
                                    value = keyToString(itemKey),
                                    selectedOperator = FilterOperator.EQ
                                )
                            } else filterState.copy(value = keyToString(itemKey))
                            onStateChange(updatedFilterState)
                            expanded = false
                        }
                    )
                    if (index < optionsMap.size - 1) HorizontalDivider()
                }
            }
        }

        Box(modifier = Modifier.padding(start = 7.dp)) {
            OutlinedButton(
                onClick = { if (operatorButtonEnabled) expandedOperators = true },
                enabled = operatorButtonEnabled,
                modifier = Modifier.defaultMinSize(minWidth = 60.dp),

                border = when {
                    filterState.isEnabled && operatorButtonEnabled && currentActiveColor != null -> BorderStroke(
                        1.dp,
                        currentActiveColor
                    )

                    filterState.isEnabled && !operatorButtonEnabled -> ButtonDefaults.outlinedButtonBorder
                    !filterState.isEnabled -> null
                    else -> ButtonDefaults.outlinedButtonBorder
                },
                colors = if (currentActiveColor != null && operatorButtonEnabled) {
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = currentActiveColor
                    )
                } else if (currentActiveColor != null && !operatorButtonEnabled) {
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DefaultAlpha)
                    )
                } else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(filterState.selectedOperator.symbol, maxLines = 1)
            }
            DropdownMenu(
                expanded = expandedOperators,
                onDismissRequest = { expandedOperators = false }
            ) {
                filterState.availableOperators.forEach { operator ->
                    DropdownMenuItem(
                        text = { Text("${operator.symbol} ${operator.displayName}") },
                        onClick = {
                            onStateChange(filterState.copy(selectedOperator = operator))
                            expandedOperators = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterableBooleanChoiceField(
    label: String,
    filterState: FieldFilterState,
    onStateChange: (FieldFilterState) -> Unit,
    activeColor: Color? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentActiveColor = if (filterState.isEnabled) activeColor else null
    val booleanOptions = mapOf("true" to "Да", "false" to "Нет")

    val currentFilterState = filterState.copy(
        selectedOperator = FilterOperator.EQ,
        availableOperators = listOf(FilterOperator.EQ)
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = currentFilterState.isEnabled,
            onCheckedChange = { isChecked ->
                val newValue = if (!isChecked) "" else currentFilterState.value
                onStateChange(
                    currentFilterState.copy(
                        isEnabled = isChecked,
                        value = newValue
                    )
                )
            }
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (currentFilterState.isEnabled) expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = booleanOptions[filterState.value] ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = currentFilterState.isEnabled,
                colors = if (currentActiveColor != null) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentActiveColor,
                        unfocusedBorderColor = if (currentFilterState.isEnabled) currentActiveColor else OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                        focusedLabelColor = currentActiveColor,
                        focusedTrailingIconColor = currentActiveColor
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                booleanOptions.forEach { (boolValue, displayText) ->
                    DropdownMenuItem(
                        text = { Text(displayText) },
                        onClick = {
                            onStateChange(currentFilterState.copy(value = boolValue))
                            expanded = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.padding(start = 7.dp)) {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.defaultMinSize(minWidth = 60.dp),
                border = when {
                    currentFilterState.isEnabled -> ButtonDefaults.outlinedButtonBorder
                    !currentFilterState.isEnabled -> null
                    else -> ButtonDefaults.outlinedButtonBorder
                },
                colors = if (currentActiveColor != null) ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DefaultAlpha)
                ) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(filterState.selectedOperator.symbol, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterableDateField(
    label: String,
    filterState: FieldFilterState,
    onStateChange: (FieldFilterState) -> Unit,
    activeColor: Color? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedOperators by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val currentActiveColor = if (filterState.isEnabled) activeColor else null
    var displayValue by remember(filterState.value) {
        mutableStateOf(
            TextFieldValue(
                text = try {
                    if (filterState.value.isNotBlank()) {
                        val localeDate = LocalDate.parse(filterState.value)
                        DateTimeFormatters.ddMMyyFormatter.format(localeDate)
                    } else ""
                } catch (_: Exception) {
                    filterState.value
                },
                selection = TextRange(
                    try {
                        if (filterState.value.isNotBlank()) {
                            val localeDate = LocalDate.parse(filterState.value)
                            DateTimeFormatters.ddMMyyFormatter.format(localeDate).length
                        } else 0
                    } catch (_: Exception) {
                        filterState.value.length
                    }
                )
            )
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = filterState.isEnabled,
            onCheckedChange = { isChecked ->
                if (!isChecked) {
                    focusManager.clearFocus()
                    displayValue = TextFieldValue("")
                    onStateChange(
                        filterState.copy(
                            isEnabled = isChecked,
                            value = "",
                            errorMessage = null
                        )
                    )
                } else {
                    onStateChange(filterState.copy(isEnabled = isChecked))
                }
            }
        )

        OutlinedTextField(
            value = displayValue,
            onValueChange = { newValue ->
                val digitsOnly = newValue.text.filter { it.isDigit() }.take(8)

                val formattedValue = when {
                    digitsOnly.length <= 2 -> digitsOnly
                    digitsOnly.length <= 4 -> "${digitsOnly.take(2)}.${digitsOnly.drop(2)}"
                    digitsOnly.length <= 8 -> "${digitsOnly.take(2)}.${digitsOnly.slice(2..3)}.${digitsOnly.drop(4)}"
                    else -> digitsOnly.take(8).let { "${it.take(2)}.${it.slice(2..3)}.${it.drop(4)}" }
                }

                val newCursorPosition = formattedValue.length

                displayValue = TextFieldValue(
                    text = formattedValue,
                    selection = TextRange(newCursorPosition)
                )

                val validatorError = Validators.dateDdMmYyValidator(formattedValue)

                if (validatorError == null && formattedValue.isNotBlank()) {
                    try {
                        val parseDate = DateTimeFormatters.ddMMyyFormatter.parse(formattedValue)
                        onStateChange(
                            filterState.copy(
                                value = parseDate.toString(),
                                errorMessage = null
                            )
                        )
                    } catch (_: Exception) {
                        onStateChange(
                            filterState.copy(
                                value = "",
                                errorMessage = "Неверный формат даты"
                            )
                        )
                    }
                } else if (formattedValue.isBlank()) {
                    onStateChange(
                        filterState.copy(
                            value = "",
                            errorMessage = null
                        )
                    )
                } else {
                    onStateChange(
                        filterState.copy(
                            value = "",
                            errorMessage = validatorError
                        )
                    )
                }
            },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            enabled = filterState.isEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = filterState.errorMessage != null && filterState.value.isNotBlank(),
            supportingText = {
                if (filterState.errorMessage != null && displayValue.text.isNotBlank()) {
                    Text(filterState.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = { if (filterState.isEnabled) showDatePicker = true },
                    enabled = filterState.isEnabled
                ) {
                    Icon(Icons.Filled.DateRange, "Выбрать дату")
                }
            },

            colors = if (currentActiveColor != null) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentActiveColor,
                    unfocusedBorderColor = if (filterState.isEnabled) currentActiveColor else OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                    focusedLabelColor = currentActiveColor,
                    focusedTrailingIconColor = currentActiveColor
                )
            } else {
                OutlinedTextFieldDefaults.colors()
            }
        )
        if (showDatePicker && filterState.isEnabled) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val todayMillis = today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

            val yearRange = (today.year - 100)..(today.year + 5)
            val initialDateMillis: Long? = filterState.value.takeIf { it.isNotBlank() }
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
                yearRange = (today.year - 100)..(today.year + 5),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= todayMillis
                    }
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            if (millis <= todayMillis) {
                                val selectedInstant = Instant.fromEpochMilliseconds(millis)
                                val selectedLocalDate =
                                    selectedInstant.toLocalDateTime(TimeZone.UTC).date
                                val newIsoValue = selectedLocalDate.toString()
                                onStateChange(
                                    filterState.copy(
                                        value = newIsoValue,
                                        errorMessage = null
                                    )
                                )
                                displayValue = TextFieldValue(
                                    text = DateTimeFormatters.ddMMyyFormatter.format(selectedLocalDate),
                                    selection = TextRange(DateTimeFormatters.ddMMyyFormatter.format(selectedLocalDate).length)
                                )
                            } else {
                                onStateChange(
                                    filterState.copy(
                                        errorMessage = "Дата не может быть больше текущей"
                                    )
                                )
                            }
                        }
                    }) {
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Box(modifier = Modifier.padding(start = 7.dp)) {
            OutlinedButton(
                onClick = { expandedOperators = true },
                enabled = filterState.isEnabled,
                modifier = Modifier.defaultMinSize(minWidth = 60.dp),
                border = if (currentActiveColor != null) BorderStroke(
                    1.dp,
                    currentActiveColor
                ) else null,
                colors = if (currentActiveColor != null) ButtonDefaults.outlinedButtonColors(
                    contentColor = currentActiveColor
                ) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(filterState.selectedOperator.symbol, maxLines = 1)
            }
            DropdownMenu(
                expanded = expandedOperators,
                onDismissRequest = { expandedOperators = false }
            ) {
                filterState.availableOperators.forEach { operator ->
                    DropdownMenuItem(
                        text = { Text("${operator.symbol} ${operator.displayName}") },
                        onClick = {
                            onStateChange(filterState.copy(selectedOperator = operator))
                            expandedOperators = false
                        }
                    )
                }
            }
        }
    }
}