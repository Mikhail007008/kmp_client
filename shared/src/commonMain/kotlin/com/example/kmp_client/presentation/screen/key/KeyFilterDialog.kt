package com.example.kmp_client.presentation.screen.key

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
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.kmp_client.domain.model.key.KeyFiltersModels
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kmp_client.domain.model.key.KeyType
import com.example.kmp_client.domain.util.Validators
import com.example.kmp_client.presentation.ui.component.FilterableBooleanChoiceField
import com.example.kmp_client.presentation.ui.component.FilterableDateField
import com.example.kmp_client.presentation.ui.component.FilterableDropdownField
import com.example.kmp_client.presentation.ui.component.FilterableTextField
import kmp_eraclient.shared.generated.resources.Res
import kmp_eraclient.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun KeyFilterDialog(
    activeKeyFilters: KeyFiltersModels,
    onDismissRequest: () -> Unit,
    onApplyFilters: (KeyFiltersModels) -> Unit,
    onClearFilters: () -> Unit,
    onCancelButtonClick: (() -> Unit),
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    var currentDialogStateFilters by remember {
        mutableStateOf(activeKeyFilters)
    }

    LaunchedEffect(activeKeyFilters) {
        if (currentDialogStateFilters != activeKeyFilters)
        { currentDialogStateFilters = activeKeyFilters }
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
                    stringResource(Res.string.key_filters),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(15.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    val activeFieldColor = MaterialTheme.colorScheme.primary

                    FilterableTextField(
                        label = stringResource(Res.string.key_uid),
                        filterState = currentDialogStateFilters.uid,
                        onStateChange = { updatedState ->
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                uid = updatedState.copy(
                                    errorMessage = Validators.maxLength(20)(updatedState.value)
                                        ?: if (updatedState.value.any { !it.isDigit() } && updatedState.value.isNotBlank()) "Только числовой формат" else null
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.uid.isEnabled) activeFieldColor else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        inputSanitizer = { _, newValue ->
                            newValue.filter { it.isDigit() }.take(20)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    val keyTypeOptions = remember {
                        KeyType.entries.associate { it.name to it.displayName }
                    }

                    FilterableDropdownField(
                        label = stringResource(Res.string.key_type),
                        filterState = currentDialogStateFilters.keyType,
                        optionsMap = keyTypeOptions,
                        keyToString = { keyString -> keyString },
                        stringToKey = { valueString -> valueString },
                        onStateChange = { updatedState ->
                            currentDialogStateFilters =
                                currentDialogStateFilters.copy(keyType = updatedState)
                        },
                        activeColor = if (currentDialogStateFilters.keyType.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableDateField(
                        label = stringResource(Res.string.valid_until),
                        filterState = currentDialogStateFilters.expiration,
                        onStateChange = { updatedState ->
                            currentDialogStateFilters =
                                currentDialogStateFilters.copy(expiration = updatedState)
                        },
                        activeColor = if (currentDialogStateFilters.expiration.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableBooleanChoiceField(
                        label = stringResource(Res.string.key_blocked),
                        filterState = currentDialogStateFilters.blocked,
                        onStateChange = { updatedState ->
                            currentDialogStateFilters =
                                currentDialogStateFilters.copy(blocked = updatedState)
                        },
                        activeColor = if (currentDialogStateFilters.blocked.isEnabled) activeFieldColor else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 7.dp))

                    FilterableTextField(
                        label = stringResource(Res.string.comment),
                        filterState = currentDialogStateFilters.description,
                        onStateChange = { updatedState ->
                            currentDialogStateFilters = currentDialogStateFilters.copy(
                                description = updatedState.copy(
                                    errorMessage = Validators.maxLength(30)(updatedState.value)
                                )
                            )
                        },
                        activeColor = if (currentDialogStateFilters.description.isEnabled) activeFieldColor else null,
                        inputSanitizer = { _, newValue -> newValue.take(30) }
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
                        currentDialogStateFilters = KeyFiltersModels()
                        onClearFilters()
                    }) {

                        Text(stringResource(Res.string.clear), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(7.dp))

                    Button(
                        onClick = {
                            val hasErrors = currentDialogStateFilters.allFieldsStates
                                .any { it.isEnabled && it.errorMessage != null }
                            if (!hasErrors) {
                                onApplyFilters(currentDialogStateFilters)
                                onDismissRequest()
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Ошибка в фильтрировании") }
                            }
                        },
                        enabled = currentDialogStateFilters.allFieldsStates.none { it.isEnabled && it.errorMessage != null } &&
                                currentDialogStateFilters.hasActiveFilters()
                    ) {
                        Text(stringResource(Res.string.apply), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}