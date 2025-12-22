package com.example.kmp_client.presentation.screen.eventLog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.device.Device
import com.example.kmp_client.domain.model.event.EventFilters
import com.example.kmp_client.domain.model.event.EventTypeFilter
import kmp_eraclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFilterDialog(
    showDialog: Boolean,
    allDevice: List<Device>,
    currentFilterState: EventFilters,
    onDismissRequest: () -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onDeviceSelectionChanged: (deviceId: String, isSelected: Boolean) -> Unit,
    onEventTypeSelected: (eventTypeDisplayName: String) -> Unit,
    onSelectAllDevicesChanged: (selectAll: Boolean) -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(Res.string.event_filters)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(stringResource(Res.string.controllers), style = MaterialTheme.typography.titleMedium)

                    if (allDevice.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val currentlyAllSelected =
                                        currentFilterState.selectedDeviceIds.size == allDevice.size
                                    onSelectAllDevicesChanged(!currentlyAllSelected)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = currentFilterState.selectedDeviceIds.size == allDevice.size && allDevice.isNotEmpty(),
                                onCheckedChange = { isChecked -> onSelectAllDevicesChanged(isChecked) }
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(stringResource(Res.string.select_all))
                        }
                    }
                    Spacer(modifier = Modifier.height(7.dp))

                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn {
                            items(allDevice, key = { it.id }) { device ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDeviceSelectionChanged(
                                                device.id,
                                                !currentFilterState.selectedDeviceIds.contains(
                                                    device.id
                                                )
                                            )
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = currentFilterState.selectedDeviceIds.contains(
                                            device.id
                                        ),
                                        onCheckedChange = { isChecked ->
                                            onDeviceSelectionChanged(device.id, isChecked)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Text(device.name.ifEmpty { device.id })
                                }
                            }

                            if (allDevice.isEmpty()) item { Text(stringResource(Res.string.devices_list_is_empty)) }
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))

                    Text(stringResource(Res.string.event_type), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(7.dp))

                    var eventTypeExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = eventTypeExpanded,
                        onExpandedChange = { eventTypeExpanded = !eventTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentFilterState.selectedEventType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.select_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = eventTypeExpanded,
                            onDismissRequest = { eventTypeExpanded = false }
                        ) {
                            EventTypeFilter.entries.forEach { eventType ->
                                DropdownMenuItem(
                                    text = { Text(eventType.displayName) },
                                    onClick = {
                                        onEventTypeSelected(eventType.displayName)
                                        eventTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))

                    Button(
                        onClick = onClearFilters,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.clear))
                    }
                }
            },
            confirmButton = {
                Button(onClick = onApplyFilters) {
                    Text(stringResource(Res.string.apply_changes))
                }
            },
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}