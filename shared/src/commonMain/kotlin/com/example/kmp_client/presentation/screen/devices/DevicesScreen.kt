package com.example.kmp_client.presentation.screen.devices

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.device.Device
import com.example.kmp_client.presentation.ui.component.CustomVerticalScrollbarForLazyList
import kmp_eraclient.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    snackbarHostState: SnackbarHostState
) {
    val viewModel: DevicesViewModel = koinInject()
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

        if (uiState.isLoading && uiState.devices.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                DeviceList(
                    devices = uiState.devices,
                    containerHeight = containerHeight - 80.dp,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.loadDevices() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(Res.string.refresh))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.refresh_list))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    containerHeight: Dp,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    if (devices.isEmpty()){
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.no_devices_or_not_download))
        }
        return
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier,
            contentPadding = PaddingValues(15.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = devices,
                key = { device -> device.id }) { device ->
                DeviceItem(device = device)
                if (devices.lastOrNull() != device) {
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                }
            }
        }

        CustomVerticalScrollbarForLazyList(
            lazyListState = lazyListState,
            containerHeight = containerHeight,
            totalItemsCount = devices.size,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun DeviceItem(device: Device) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(15.dp)
        ) {
            DeviceDetailRow(stringResource(Res.string.mac_address), device.id)
            DeviceDetailRow(stringResource(Res.string.model), device.modelType.displayName)
            DeviceDetailRow(stringResource(Res.string.device_name), device.name)
            DeviceDetailRow(stringResource(Res.string.network_address), "${device.ipAddress}:${device.port}")
            DeviceDetailRow(stringResource(Res.string.synchronize), if (device.isSynchronized) stringResource(Res.string.yes) else stringResource(Res.string.no))
            DeviceDetailRow(stringResource(Res.string.mode), device.operatingMode.displayName)
        }
    }
}

@Composable
private fun DeviceDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}