package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kmp_client.domain.model.user.UserFilterModels
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import com.example.kmp_client.domain.util.Validators
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.kmp_client.presentation.ui.component.FilterableBooleanChoiceField
import com.example.kmp_client.presentation.ui.component.FilterableDateField
import com.example.kmp_client.presentation.ui.component.FilterableDropdownField
import com.example.kmp_client.presentation.ui.component.FilterableTextField
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun FilterDialog(
    userFilterModels: UserFilterModels,
    jobTitlesMap: Map<Long, String>,
    departmentsMap: Map<Long, String>,
    onDismissRequest: () -> Unit,
    onApplyFilters: (UserFilterModels) -> Unit,
    onClearFilters: () -> Unit,
    onCancelButtonClick: (() -> Unit),
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
) {
    var currentDialogStateFilters by remember { mutableStateOf(userFilterModels) }

    LaunchedEffect(userFilterModels) {
        currentDialogStateFilters = userFilterModels
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    stringResource(Res.string.filters),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 15.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    val activeFieldColor = MaterialTheme.colorScheme.primary

                    FilterableTextField(
                        label = stringResource(Res.string.employee_number),
                        filterState = currentDialogStateFilters.code,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                code = it.copy(
                                    errorMessage = Validators.maxLength(30)(it.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.code.isEnabled) activeFieldColor else null,
                        inputSanitizer = { _, newValue -> newValue.take(30) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.surname),
                        filterState = currentDialogStateFilters.surname,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                surname = it.copy(
                                    errorMessage = Validators.maxLength(30)(it.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.surname.isEnabled) activeFieldColor else null,
                        inputSanitizer = { _, newValue -> newValue.take(30) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.name),
                        filterState = currentDialogStateFilters.name,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                name = it.copy(
                                    errorMessage = Validators.maxLength(30)(it.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.name.isEnabled) activeFieldColor else null,
                        inputSanitizer = { _, newValue -> newValue.take(30) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.patronymic),
                        filterState = currentDialogStateFilters.patronymic,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                patronymic = it.copy(
                                    errorMessage = Validators.maxLength(30)(it.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.patronymic.isEnabled) activeFieldColor else null,
                        inputSanitizer = { _, newValue -> newValue.take(30) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))
                    if (currentDialogStateFilters.featuresFilters.isNotEmpty()) {
                        currentDialogStateFilters.featuresFilters.forEachIndexed { index, featureFilterState ->
                            FilterableTextField(
                                label = featureFilterState.displayName,
                                filterState = featureFilterState,
                                onStateChange = { updatedFeatureState ->
                                    val newList =
                                        currentDialogStateFilters.featuresFilters.toMutableList()
                                    newList[index] = updatedFeatureState.copy(
                                        errorMessage = Validators.maxLength(50)(updatedFeatureState.value)
                                    )
                                    currentDialogStateFilters =
                                        currentDialogStateFilters.copy(featuresFilters = newList)
                                },
                                activeColor = if (featureFilterState.isEnabled) activeFieldColor else null,
                                inputSanitizer = { _, newValue -> newValue.take(50) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))
                        }
                    }

                    FilterableDropdownField(
                        label = stringResource(Res.string.job_title),
                        filterState = currentDialogStateFilters.jobTitleId,
                        optionsMap = jobTitlesMap,
                        keyToString = { keyLong -> keyLong.toString() },
                        stringToKey = { valueString -> valueString.toLongOrNull() },
                        onStateChange = {
                            currentDialogStateFilters =
                                currentDialogStateFilters.copy(jobTitleId = it)
                        },
                        activeColor = if (currentDialogStateFilters.jobTitleId.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableDropdownField(
                        label = stringResource(Res.string.department),
                        filterState = currentDialogStateFilters.departmentId,
                        optionsMap = departmentsMap,
                        keyToString = { keyLong -> keyLong.toString() },
                        stringToKey = { valueString -> valueString.toLongOrNull() },
                        onStateChange = {
                            currentDialogStateFilters =
                                currentDialogStateFilters.copy(departmentId = it)
                        },
                        activeColor = if (currentDialogStateFilters.departmentId.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.phone),
                        filterState = currentDialogStateFilters.phone,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                phone = it.copy(
                                    errorMessage = Validators.phoneValidator(it.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.phone.isEnabled) activeFieldColor else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        inputSanitizer = { _, newValue -> newValue.take(20) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.email),
                        filterState = currentDialogStateFilters.email,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                email = it.copy(
                                    errorMessage = Validators.maxLength(30)(it.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.email.isEnabled) activeFieldColor else null,
                        inputSanitizer = { _, newValue -> newValue.take(30) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.balance),
                        filterState = currentDialogStateFilters.balance,
                        onStateChange = { updatedState ->
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                balance = updatedState.copy(
                                    errorMessage = Validators.balanceValidator(
                                        updatedState.value
                                    )
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.balance.isEnabled) activeFieldColor else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        inputSanitizer = { currentValue, newValue ->
                            var tempValue = newValue

                            val maxInputLength = 10
                            if (tempValue.length > maxInputLength) {
                                tempValue = tempValue.take(maxInputLength)
                            }

                            val sb = StringBuilder()
                            var hasDecimal = false
                            var hasMinus = false

                            for (char in tempValue) {
                                when {
                                    char.isDigit() -> sb.append(char)
                                    char == '-' && sb.isEmpty() && !hasMinus -> {
                                        sb.append(char)
                                        hasMinus = true
                                    }

                                    char == '.' && !hasDecimal -> {
                                        if (sb.isEmpty()) sb.append('0')
                                        else if (sb.length == 1 && sb.startsWith('-')) sb.insert(
                                            0,
                                            '0'
                                        )

                                        sb.append(char)
                                        hasDecimal = true
                                    }
                                }
                            }
                            tempValue = sb.toString()

                            val parts = tempValue.split('.')
                            if (parts.size == 2 && parts[1].length > 2) {
                                tempValue = "${parts[0]}.${parts[1].take(2)}"
                            }

                            val mainPart =
                                if (tempValue.contains('.')) tempValue.substringBefore('.') else tempValue
                            val digitsBeforeDecimal = mainPart.filter { it.isDigit() }
                            if (digitsBeforeDecimal.length > 6) {
                                return@FilterableTextField currentValue
                            }

                            tempValue
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableBooleanChoiceField(
                        label = stringResource(Res.string.guest),
                        filterState = currentDialogStateFilters.guest,
                        onStateChange = {
                            currentDialogStateFilters = currentDialogStateFilters.copy(guest = it)
                        },
                        activeColor = if (currentDialogStateFilters.guest.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableBooleanChoiceField(
                        label = stringResource(Res.string.photo),
                        filterState = currentDialogStateFilters.photo,
                        onStateChange = {
                            currentDialogStateFilters =
                                currentDialogStateFilters.copy(photo = it)
                        },
                        activeColor = if (currentDialogStateFilters.photo.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableDateField(
                        label = stringResource(Res.string.creation_time),
                        filterState = currentDialogStateFilters.creationTime,
                        onStateChange = { updatedFieldState ->
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                creationTime = updatedFieldState
                            )
                        },
                        activeColor = if (currentDialogStateFilters.creationTime.isEnabled) activeFieldColor else null
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (true) {
                        TextButton(onClick = {
                            onCancelButtonClick()
                            onDismissRequest()
                        }) {
                            Text(stringResource(Res.string.cancel), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                    }
                    TextButton(onClick = {
                        currentDialogStateFilters = UserFilterModels()
                        onClearFilters()
                    }) {
                        Text(stringResource(Res.string.clear), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Button(
                        onClick = {
                            val hasErrors = currentDialogStateFilters.allStandardFieldsStates
                                .any { it.isEnabled && it.errorMessage != null }

                            if (!hasErrors) {
                                onApplyFilters(currentDialogStateFilters)
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Исправьте ошибки в фильтрах") }
                            }
                        },
                        enabled = currentDialogStateFilters.allStandardFieldsStates.none { it.isEnabled && it.errorMessage != null } &&
                                currentDialogStateFilters.hasActiveFilters()
                    ) {
                        Text(stringResource(Res.string.apply), fontSize = 13.sp)
                    }

                }
            }
        }
    }
}