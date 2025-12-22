package com.example.kmp_client.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kmp_client.presentation.ui.theme.DarkBackground
import com.example.kmp_client.presentation.ui.theme.LightBackground
import com.example.kmp_client.presentation.ui.theme.LightPrimary

@Composable
fun ScreenBottomAppBar(
    onFilterClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isRefreshing: Boolean,
    areFiltersActive: Boolean,
    isSelectionModeActive: Boolean,
    selectedItemsCount: Int,
    onDeleteModeToggleClick: () -> Unit,
    onCancelSelectionClick: () -> Unit,
    onApplySelectionClick: () -> Unit,
    isDeletionInProgress: Boolean,
    modifier: Modifier = Modifier,
    filterButtonLabel: String = "Фильтр",
    filterButtonIcon: ImageVector = Icons.Filled.Info,
    addItemButtonLabel: String = "Добавить",
    addItemButtonIcon: ImageVector = Icons.Filled.Add,
    applySelectionButtonLabel: String = "Удалить",
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = if (MaterialTheme.colorScheme.primary == LightPrimary) LightBackground else DarkBackground,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        BottomAppBarItem(
            label = filterButtonLabel,
            icon = filterButtonIcon,
            contentDescription = "$filterButtonLabel списка",
            onClick = onFilterClick,
            modifier = Modifier.weight(1f),
            activeColor = if (areFiltersActive) MaterialTheme.colorScheme.primary else null,
            enabled = !isSelectionModeActive && !isDeletionInProgress && !isRefreshing
        )
        if (isSelectionModeActive) {
            BottomAppBarItem(
                label = "Очистить",
                icon = Icons.Filled.Clear,
                contentDescription = "Отменить выбор",
                onClick = onCancelSelectionClick,
                modifier = Modifier.weight(1f),
                enabled = !isDeletionInProgress
            )
            BottomAppBarItem(
                label = "$applySelectionButtonLabel (${selectedItemsCount})",
                icon = Icons.Filled.Check,
                contentDescription = "Применить удаление",
                onClick = onApplySelectionClick,
                modifier = Modifier.weight(1f),
                enabled = selectedItemsCount > 0 && !isDeletionInProgress,
                activeColor = if (selectedItemsCount > 0) MaterialTheme.colorScheme.primary else null,
                isProcessing = isDeletionInProgress
            )
        } else {
            BottomAppBarItem(
                label = addItemButtonLabel,
                icon = addItemButtonIcon,
                contentDescription = "Добавить элемент",
                onClick = onAddItemClick,
                modifier = Modifier.weight(1f),
                enabled = !isDeletionInProgress && !isRefreshing
            )

            BottomAppBarItem(
                label = "Обновить",
                icon = Icons.Filled.Refresh,
                contentDescription = "Обновить список",
                onClick = onRefreshClick,
                isProcessing = isRefreshing,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                enabled = !isDeletionInProgress
            )
        }
        BottomAppBarItem(
            label = if (isSelectionModeActive) "Отмена" else "Удалить",
            icon = if (isSelectionModeActive) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Delete,
            contentDescription = if (isSelectionModeActive) "Выйти из режима удаления" else "Войти в режим удаления",
            onClick = onDeleteModeToggleClick,
            modifier = Modifier.weight(1f),
            activeColor = if (isSelectionModeActive) MaterialTheme.colorScheme.error else null,
            isProcessing = isDeletionInProgress,
            enabled = !isRefreshing
        )
    }
}

@Composable
private fun RowScope.BottomAppBarItem(
    label: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    activeColor: Color? = null,
    enabled: Boolean = true
) {
    val currentContentColor = activeColor ?: LocalContentColor.current
    val finalEnabled = enabled && !isProcessing

    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = finalEnabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.width(24.dp),
                    color = activeColor ?: MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = currentContentColor
                )
            }
            Text(
                text = label,
                fontSize = 13.sp,
                lineHeight = 14.sp,
                color = if (finalEnabled) currentContentColor else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
        }
    }
}