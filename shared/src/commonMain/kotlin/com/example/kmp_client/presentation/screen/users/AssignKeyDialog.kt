package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.kmp_client.data.remote.dto.mapper.formatExpirationForDisplay
import com.example.kmp_client.domain.model.key.Key
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun AssignKeyDialog(
    availableKeys: List<Key>,
    selectedKeyId: String?,
    onKeySelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    error: String?,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.select_key)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLoading && availableKeys.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                } else if (availableKeys.isEmpty()) {
                    Text(stringResource(Res.string.no_keys_available))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(
                            items = availableKeys,
                            key = { keyItem -> keyItem.id }
                        ) { key ->
                            KeyAssignmentItem(
                                key = key,
                                isSelected = key.id == selectedKeyId,
                                onSelected = { onKeySelected(key.id) }
                            )
                            HorizontalDivider()
                        }

                        if (canLoadMore && !isLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    OutlinedButton(onClick = onLoadMore) {
                                        Text(stringResource(Res.string.load_more))
                                    }
                                }
                            }
                        }

                        if (isLoading && availableKeys.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedKeyId != null && !isLoading
            ) {
                Text(stringResource(Res.string.select))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )

    val currentItems = listState.layoutInfo.visibleItemsInfo
    LaunchedEffect(currentItems, isLoading, canLoadMore) {
        if (!isLoading && canLoadMore) {
            val lastVisibleItem = currentItems.lastOrNull()
            if (lastVisibleItem != null && lastVisibleItem.index >= availableKeys.size - 5)
                onLoadMore()
        }
    }
}

@Composable
private fun KeyAssignmentItem(
    key: Key,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(vertical = 7.dp, horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected
        )
        Spacer(Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${stringResource(Res.string.key_colon)} ${key.id}", fontWeight = FontWeight.Bold)
            val expirationText = formatExpirationForDisplay(key.expirationInstant)
            if (expirationText.isNotBlank()) {
                Text("${stringResource(Res.string.expiration)} $expirationText")
            }
            key.comment?.let {
                if (it.isNotBlank()) {
                    Text("${stringResource(Res.string.comment_colon)} $it")
                }
            }
        }
    }
}