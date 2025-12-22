package com.example.kmp_client.presentation.screen.eventLog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kmp_client.domain.model.event.Event
import kmp_eraclient.shared.generated.resources.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import moe.tlaster.precompose.koin.koinViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventLogScreen(
    snackbarHostState: SnackbarHostState
) {
    val viewModel: EventLogViewModel = koinViewModel(EventLogViewModel::class)
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var controlsVisible by remember { mutableStateOf(true) }
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val firstVisibleItemScrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    var previousFirstVisibleItemIndex by remember { mutableStateOf(firstVisibleItemIndex) }
    var previousFirstVisibleItemScrollOffset by remember {
        mutableStateOf(
            firstVisibleItemScrollOffset
        )
    }

    LaunchedEffect(firstVisibleItemIndex, firstVisibleItemScrollOffset) {
        controlsVisible = when {
            firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0 -> true

            firstVisibleItemIndex > previousFirstVisibleItemIndex ||
                    (firstVisibleItemIndex == previousFirstVisibleItemIndex &&
                            firstVisibleItemScrollOffset > previousFirstVisibleItemScrollOffset &&
                            firstVisibleItemIndex != 0) -> false

            firstVisibleItemIndex < previousFirstVisibleItemIndex ||
                    (firstVisibleItemIndex == previousFirstVisibleItemIndex &&
                            firstVisibleItemScrollOffset < previousFirstVisibleItemScrollOffset) -> true

            else -> controlsVisible
        }

        previousFirstVisibleItemIndex = firstVisibleItemIndex
        previousFirstVisibleItemScrollOffset = firstVisibleItemScrollOffset
    }


    uiState.isDatePickerVisible?.let { datePickerTarget ->
        ShowDatePickerDialog(
            initialDate = if (datePickerTarget == DatePickerTarget.START) uiState.startDate else uiState.endDate,
            onDateSelected = { selectedDate ->
                viewModel.onDateSelected(datePickerTarget, selectedDate)
                viewModel.hidePicker(PickerType.DATE)
            },
            onDismiss = { viewModel.hidePicker(PickerType.DATE) }
        )
    }

    uiState.isTimePickerVisible?.let { timePickerTarget ->
        ShowTimePickerDialog(
            initialTime = if (timePickerTarget == TimePickerTarget.START) uiState.startTime else uiState.endTime,
            onTimeSelected = { selectedTime ->
                viewModel.onTimeSelected(timePickerTarget, selectedTime)
                viewModel.hidePicker(PickerType.TIME)
            },
            onDismiss = { viewModel.hidePicker(PickerType.TIME) }
        )
    }

    EventFilterDialog(
        showDialog = uiState.showFilterDialog,
        allDevice = uiState.allAvailableDevices,
        currentFilterState = uiState.tempEventFilters,
        onDismissRequest = { viewModel.onDismissFilterDialog() },
        onApplyFilters = { viewModel.onApplyFilters() },
        onClearFilters = { viewModel.onCLearFiltersInDialog() },
        onDeviceSelectionChanged = { deviceId, isSelected ->
            viewModel.onDeviceSelectionChangedInDialog(deviceId, isSelected)
        },
        onEventTypeSelected = { eventTypeName ->
            viewModel.onEventTypeSelectedInDialog(eventTypeName)
        },
        onSelectAllDevicesChanged = { selectAll ->
            viewModel.onSelectAllDevicesChanged(selectAll)
        }
    )


    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    DateTimeRangeSelector(
                        startDate = uiState.startDate,
                        startTime = uiState.startTime,
                        endDate = uiState.endDate,
                        endTime = uiState.endTime,
                        onShowStartDatePicker = { viewModel.showPicker(DatePickerTarget.START) },
                        onShowStartTimePicker = { viewModel.showPicker(TimePickerTarget.START) },
                        onShowEndDatePicker = { viewModel.showPicker(DatePickerTarget.END) },
                        onShowEndTimePicker = { viewModel.showPicker(TimePickerTarget.END) },
                        dateTimeValidationError = uiState.dateTimeValidationError
                    )
                    Spacer(modifier = Modifier.height(7.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onOpenFilterDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.filter), color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.loadEvents(isInitialLoad = true) },
                            enabled = !uiState.isLoading && uiState.dateTimeValidationError == null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.show), color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(7.dp))

                    Button(
                        onClick = { viewModel.exportEventsToCsv() },
                        enabled = uiState.canSave && !uiState.isExporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (uiState.isExporting) stringResource(Res.string.saving) else stringResource(Res.string.save_result),
                            color = Color.White
                        )
                    }
                    
                    uiState.exportError?.let { errorMessage ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            color = if (errorMessage.contains("Успешно")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading && uiState.events.isEmpty()) {
                    CircularProgressIndicator()
                } else if (uiState.error != null && uiState.events.isEmpty()) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(15.dp)
                    )
                } else if (uiState.events.isEmpty() && !uiState.isLoading) {
                    Text(
                        text = stringResource(Res.string.no_events),
                        color = Color.White,
                        modifier = Modifier.padding(15.dp)
                    )
                }
            }

            if ((!uiState.isLoading && uiState.error == null && uiState.events.isNotEmpty()) || uiState.events.isNotEmpty()) {
                EventList(
                    listState = listState,
                    events = uiState.events,
                    isLoadingMore = uiState.isLoadingMore,
                    canLoadMore = uiState.canLoadMore,
                    onLoadMore = { viewModel.loadEvents(isInitialLoad = false) }
                )
            }
        }
    }
}

private val datePickerFormatter =
    LocalDate.Format { dayOfMonth(); char('.'); monthNumber(); char('.'); year() }
private val timePickerFormatter = LocalTime.Format { hour(); char(':'); minute() }

@Composable
fun DateTimeRangeSelector(
    startDate: LocalDate,
    startTime: LocalTime,
    endDate: LocalDate,
    endTime: LocalTime,
    onShowStartDatePicker: () -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit,
    dateTimeValidationError: String?
) {
    Column {
        Text(stringResource(Res.string.period), color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.from), modifier = Modifier.padding(end = 8.dp), color = Color.White)
            OutlinedButton(
                onClick = onShowStartDatePicker,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату начала")
                Spacer(Modifier.width(4.dp))
                Text(startDate.format(datePickerFormatter))
            }
            Spacer(Modifier.width(7.dp))
            OutlinedButton(
                onClick = onShowStartTimePicker,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(startTime.format(timePickerFormatter))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.to), modifier = Modifier.padding(end = 4.dp), color = Color.White)
            OutlinedButton(
                onClick = onShowEndDatePicker,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату окончания")
                Spacer(Modifier.width(4.dp))
                Text(endDate.format(datePickerFormatter))
            }
            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = onShowEndTimePicker,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(endTime.format(timePickerFormatter))
            }
        }

        if (dateTimeValidationError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                dateTimeValidationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun EventList(
    listState: LazyListState,
    events: List<Event>,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(events, key = { it.id }) { event ->
            EventItem(event)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        if (canLoadMore && !isLoadingMore) {
            item {
                Button(
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)
                ) {
                    Text(stringResource(Res.string.load_more), color = Color.White)
                }
            }
        }
        if (isLoadingMore) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    val currentItemsInfo = listState.layoutInfo.visibleItemsInfo
    LaunchedEffect(currentItemsInfo, isLoadingMore, canLoadMore) {
        if (!isLoadingMore && canLoadMore && events.isNotEmpty()) {
            val lastVisibleItem = currentItemsInfo.lastOrNull()
            if (lastVisibleItem != null && lastVisibleItem.index >= events.size - 5) {
                onLoadMore()
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(15.dp)
    ) {
        Text("${stringResource(Res.string.date_time)} ${event.formattedDateTime}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("${stringResource(Res.string.controller)} ${event.deviceName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Text("${stringResource(Res.string.event)} ${event.eventDescription}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "${stringResource(Res.string.reader)} ${event.readerDescription}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text("${stringResource(Res.string.user)} ${event.userFullName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Text("${stringResource(Res.string.key)} ${event.keyNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
