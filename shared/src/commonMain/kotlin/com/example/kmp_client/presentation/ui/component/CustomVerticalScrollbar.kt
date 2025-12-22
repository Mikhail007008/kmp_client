package com.example.kmp_client.presentation.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CustomVerticalScrollbarForLazyList(
    lazyListState: LazyListState,
    containerHeight: Dp,
    totalItemsCount: Int,
    modifier: Modifier = Modifier,
    scrollbarWidth: Dp = 4.dp,
    scrollbarHeight: Dp = 80.dp,
    scrollbarColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    hideDelayMs: Long = 1500L,
) {
    val density = LocalDensity.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(lazyListState.isScrollInProgress, totalItemsCount) {
        if (totalItemsCount == 0) {
            isVisible = false
            return@LaunchedEffect
        }

        if (lazyListState.isScrollInProgress) {
            isVisible = true
        } else if (isVisible) {
            delay(hideDelayMs)
            if (!lazyListState.isScrollInProgress) {
                isVisible = false
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible && totalItemsCount > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(scrollbarWidth + 4.dp)
                .padding(vertical = 8.dp, horizontal = 2.dp)
        ) {
            val layoutInfo = lazyListState.layoutInfo
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val totalContentHeight = layoutInfo.totalItemsCount *
                    (layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 100)

            val scrollProgress = if (totalContentHeight > viewportHeight) {
                val currentScrollOffset = lazyListState.firstVisibleItemIndex *
                        (layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 100) +
                        lazyListState.firstVisibleItemScrollOffset
                val maxScrollOffset = totalContentHeight - viewportHeight
                (currentScrollOffset.toFloat() / maxScrollOffset.toFloat()).coerceIn(0f, 1f)
            } else 0f

            val containerHeightPx = with(density) { containerHeight.toPx() }
            val scrollbarHeightPx = with(density) { scrollbarHeight.toPx() }
            val availableSpace =
                containerHeightPx - scrollbarHeightPx - with(density) { 16.dp.toPx() }
            val scrollbarPosition = scrollProgress * availableSpace

            Box(
                modifier = Modifier
                    .offset { IntOffset(0, scrollbarPosition.roundToInt()) }
                    .width(scrollbarWidth)
                    .height(scrollbarHeight)
                    .clip(RoundedCornerShape(scrollbarWidth / 2))
                    .background(scrollbarColor)
            )
        }
    }
}