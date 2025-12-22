package com.example.kmp_client.presentation.screen.accessscheme

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.kmp_client.domain.model.accesssheme.AccessScheme
import org.koin.compose.koinInject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.kmp_client.presentation.ui.component.CustomVerticalScrollbarForLazyList
import kmp_eraclient.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun AccessSchemeScreen(
    snackbarHostState: SnackbarHostState
) {
    val viewModel: AccessSchemesViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearError()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    focusManager.clearFocus()
                }
            )
    ) {
        val containerHeight = maxHeight

        if (uiState.isLoading && uiState.accessSchemes.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AccessSchemeList(
                    accessSchemes = uiState.accessSchemes,
                    containerHeight = containerHeight - 80.dp,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.loadAccessSchemes() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                        Spacer(Modifier.width(7.dp))
                        Text(stringResource(Res.string.refresh_list))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessSchemeList(
    accessSchemes: List<AccessScheme>,
    containerHeight: Dp,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    if (accessSchemes.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.access_schemes_not_found))
        }
        return
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier,
            contentPadding = PaddingValues(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = accessSchemes,
                key = { scheme -> scheme.id }
            ) { scheme ->
                AccessSchemeItem(scheme = scheme)
            }
        }

        CustomVerticalScrollbarForLazyList(
            lazyListState = lazyListState,
            containerHeight = containerHeight,
            totalItemsCount = accessSchemes.size,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }

}

@Composable
private fun AccessSchemeItem(scheme: AccessScheme) {
    SelectionContainer {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(7.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                AccessSchemeDetailRow(stringResource(Res.string.name), scheme.name)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                AccessSchemeDetailRow(
                    stringResource(Res.string.devices_assigned),
                    if (scheme.devices.isNotEmpty()) scheme.devices.joinToString(", ") else stringResource(Res.string.no_devices)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                AccessSchemeDetailRow(
                    "\"Схема по умолчанию\" для новых сотрудников:",
                    if (scheme.defaultForNewEmployees) "Да" else "Нет"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                AccessSchemeDetailRow(
                    "\"Схема по умолчанию\" для новых гостей:",
                    if (scheme.defaultForNewGuests) "Да" else "Нет"
                )
            }
        }
    }
}

@Composable
fun AccessSchemeDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(170.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
        )
        Spacer(modifier = Modifier.height(7.dp))
    }
}