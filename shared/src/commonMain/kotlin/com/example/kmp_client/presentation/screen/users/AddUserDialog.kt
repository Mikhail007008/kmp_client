package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.kmp_client.data.remote.dto.request.UserCreationData
import com.example.kmp_client.domain.util.Validators
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    jobTitlesMap: Map<Long, String>,
    departmentsMap: Map<Long, String>,
    discoveredFeatureKeys: List<String>,
    onDismissRequest: () -> Unit,
    onConfirmAddUser: (UserCreationData) -> Unit,
    isAddingUser: Boolean,
    addUserError: String?,
) {
    var codeState by remember { mutableStateOf(AddUserState()) }
    var nameState by remember { mutableStateOf(AddUserState()) }
    var surnameState by remember { mutableStateOf(AddUserState()) }
    var patronymicState by remember { mutableStateOf(AddUserState()) }
    var jobTitleState by remember { mutableStateOf(AddUserState()) }
    var departmentState by remember { mutableStateOf(AddUserState()) }
    var phoneState by remember { mutableStateOf(AddUserState()) }
    var emailState by remember { mutableStateOf(AddUserState()) }
    var balanceState by remember { mutableStateOf(AddUserState()) }
    var showOverallValidationError by remember { mutableStateOf(false) }

    val featureStates = remember {
        mutableStateMapOf<String, AddUserState>().apply {
            discoveredFeatureKeys.forEach { key ->
                this[key] = AddUserState()
            }
        }
    }

    LaunchedEffect(discoveredFeatureKeys) {
        val currentKeys = featureStates.keys.toSet()
        val newKeys = discoveredFeatureKeys.toSet()

        (newKeys - currentKeys).forEach { key ->
            featureStates[key] = AddUserState()
        }
    }

    val allFieldsValid: () -> Boolean = {
        val surnameValid = surnameState.value.isNotBlank()

        if (!surnameValid) {
            surnameState =
                surnameState.copy(errorMessage = "Фамилия обязательна для заполнения")
            showOverallValidationError = true
            false
        } else {
            surnameState = surnameState.copy(errorMessage = null)
            showOverallValidationError = false
            true
        }
    }

    Dialog(onDismissRequest = {
        showOverallValidationError = false
        onDismissRequest()
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    stringResource(Res.string.add_user),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                addUserError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                    )
                }
                if (showOverallValidationError) {
                    Text(
                        stringResource(Res.string.fill_surname),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = codeState.value,
                        onValueChange = {
                            val sanitized = it.take(30)
                            codeState = codeState.copy(
                                value = sanitized,
                                errorMessage = Validators.maxLength(30)(sanitized)
                            )
                        },
                        label = { Text(stringResource(Res.string.employee_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = codeState.errorMessage != null,
                        supportingText = codeState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = surnameState.value,
                        onValueChange = {
                            val sanitized = it.take(30)
                            surnameState = surnameState.copy(
                                value = sanitized,
                                errorMessage = if (sanitized.isBlank()) "Фамилия обязательна для заполнения" else Validators.maxLength(
                                    30
                                )(sanitized)
                            )
                        },
                        label = { Text(stringResource(Res.string.surname)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = surnameState.errorMessage != null,
                        supportingText = surnameState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nameState.value,
                        onValueChange = {
                            val sanitized = it.take(30)
                            nameState = nameState.copy(
                                value = sanitized,
                                errorMessage = Validators.maxLength(30)(sanitized)
                            )
                        },
                        label = { Text(stringResource(Res.string.name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameState.errorMessage != null,
                        supportingText = nameState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = patronymicState.value,
                        onValueChange = {
                            val sanitized = it.take(30)
                            patronymicState = patronymicState.copy(
                                value = sanitized,
                                errorMessage = Validators.maxLength(30)(sanitized)
                            )
                        },
                        label = { Text(stringResource(Res.string.patronymic)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = patronymicState.errorMessage != null,
                        supportingText = patronymicState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    SimpleDropdownMenuBox(
                        label = stringResource(Res.string.position),
                        optionsMap = jobTitlesMap,
                        selectedId = jobTitleState.selectedId,
                        onSelectionChange = { id, _ ->
                            jobTitleState =
                                jobTitleState.copy(selectedId = id, value = id?.toString() ?: "")
                        },
                        errorMessage = jobTitleState.errorMessage
                    )
                    Spacer(Modifier.height(10.dp))

                    SimpleDropdownMenuBox(
                        label = stringResource(Res.string.department),
                        optionsMap = departmentsMap,
                        selectedId = departmentState.selectedId,
                        onSelectionChange = { id, _ ->
                            departmentState =
                                departmentState.copy(selectedId = id, value = id?.toString() ?: "")
                        },
                        errorMessage = departmentState.errorMessage
                    )
                    Spacer(Modifier.height(10.dp))

                    discoveredFeatureKeys.forEach { featureKey ->
                        val currentFeatureState = featureStates[featureKey] ?: AddUserState()
                        val displayName = "Свойство ${featureKey.removePrefix("feature")}"

                        OutlinedTextField(
                            value = currentFeatureState.value,
                            onValueChange = { newValue ->
                                val sanitizedValue = newValue.take(50)
                                featureStates[featureKey] = currentFeatureState.copy(
                                    value = sanitizedValue,
                                    errorMessage = Validators.maxLength(50)(sanitizedValue)
                                )
                            },
                            label = { Text(displayName) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = currentFeatureState.errorMessage != null,
                            supportingText = currentFeatureState.errorMessage?.let { msg ->
                                {
                                    Text(
                                        msg,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = phoneState.value,
                        onValueChange = {
                            val sanitized =
                                it.filter { char -> char.isDigit() || char == '+' || char == '(' || char == ')' || char == '-' || char.isWhitespace() }
                                    .take(20)
                            phoneState = phoneState.copy(
                                value = sanitized,
                                errorMessage = Validators.phoneValidator(sanitized)
                            )
                        },
                        label = { Text(stringResource(Res.string.phone)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneState.errorMessage != null,
                        supportingText = phoneState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailState.value,
                        onValueChange = {
                            val sanitized = it.take(30)
                            emailState = emailState.copy(
                                value = sanitized,
                                errorMessage = Validators.maxLength(30)(sanitized)
                            )
                        },
                        label = { Text(stringResource(Res.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailState.errorMessage != null,
                        supportingText = emailState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = balanceState.value,
                        onValueChange = { newValue ->
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
                                        sb.append(char); hasMinus = true
                                    }

                                    char == '.' && !hasDecimal -> {
                                        if (sb.isEmpty()) sb.append('0')
                                        else if (sb.length == 1 && sb.startsWith('-')) sb.insert(
                                            0,
                                            '0'
                                        )
                                        sb.append(char); hasDecimal = true
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
                            if (digitsBeforeDecimal.length <= 6 || balanceState.value.length >= tempValue.length) {
                                balanceState = balanceState.copy(
                                    value = tempValue,
                                    errorMessage = Validators.balanceValidator(tempValue)
                                )
                            }
                        },
                        label = { Text(stringResource(Res.string.balance)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = balanceState.errorMessage != null,
                        supportingText = balanceState.errorMessage?.let {
                            {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showOverallValidationError = false
                            onDismissRequest()
                        },
                        enabled = !isAddingUser
                    ) { Text(stringResource(Res.string.cancel)) }
                    Spacer(modifier = Modifier.width(7.dp))

                    Button(
                        onClick = {
                            if (allFieldsValid()) {
                                val featuresData = featureStates
                                    .filter { (_, state) -> state.value.isNotBlank() }
                                    .mapValues { (_, state) -> state.value }
                                val userData = UserCreationData(
                                    code = codeState.value.takeIf { it.isNotBlank() },
                                    name = nameState.value.takeIf { it.isNotBlank() },
                                    surname = surnameState.value,
                                    patronymic = patronymicState.value.takeIf { it.isNotBlank() },
                                    jobTitleId = jobTitleState.selectedId,
                                    departmentId = departmentState.selectedId,
                                    features = if (featuresData.isNotEmpty()) featuresData else null,
                                    phone = phoneState.value.takeIf { it.isNotBlank() },
                                    email = emailState.value.takeIf { it.isNotBlank() },
                                    balance = balanceState.value.toDoubleOrNull()
                                )
                                onConfirmAddUser(userData)
                            }
                        },
                        enabled = !isAddingUser
                    ) {
                        if (isAddingUser) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(Res.string.add))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdownMenuBox(
    label: String,
    optionsMap: Map<Long, String>,
    selectedId: Long?,
    onSelectionChange: (id: Long?, name: String?) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOptionName = selectedId?.let { optionsMap[it] } ?: ""

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOptionName,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                isError = errorMessage != null,
                supportingText = errorMessage?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.not_selected),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        onSelectionChange(null, null)
                        expanded = false
                    }
                )

                if (optionsMap.isNotEmpty()) {
                    HorizontalDivider()
                }

                val sortedOptions = optionsMap.entries.sortedBy { it.value }
                sortedOptions.forEachIndexed { index, entry ->
                    val (id, name) = entry
                    DropdownMenuItem(
                        text = { Text(name, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onSelectionChange(id, name)
                            expanded = false
                        }
                    )

                    if (index < sortedOptions.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}