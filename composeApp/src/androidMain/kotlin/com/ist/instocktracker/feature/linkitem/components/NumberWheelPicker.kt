package com.ist.instocktracker.feature.linkitem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ist.instocktracker.Cobalt
import com.ist.instocktracker.InkFaint
import com.ist.instocktracker.InkMuted
import com.ist.instocktracker.NavDark
import com.ist.instocktracker.PrimaryTint08
import com.ist.instocktracker.PrimaryTint18
import com.ist.instocktracker.White
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val WheelRowHeight = 56.dp
private const val WheelVisibleRowCount = 5

/**
 * A vertically-scrolling, center-snapping list of integers within [range].
 *
 * The selection highlight is a fixed pill at the vertical center of the viewport — the list
 * scrolls underneath it. [selectedValue]/[onValueChange] make this a controlled component;
 * [onValueChange] fires once the list settles on a new centered value, not on every scroll delta.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberWheel(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = WheelRowHeight,
    visibleRowCount: Int = WheelVisibleRowCount,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val rowHeightPx = with(density) { rowHeight.toPx() }
    val paddingRows = visibleRowCount / 2
    val viewportHeight = rowHeight * visibleRowCount

    fun indexForValue(value: Int) = (value - range.first).coerceIn(0, range.count() - 1)

    // Item 0 rests centered under the fixed pill by construction (top contentPadding equals
    // paddingRows * rowHeight), so scrolling `index * rowHeightPx` further from that rest state
    // centers item `index` — this avoids depending on scrollToItem's per-item offset semantics.
    LaunchedEffect(range) {
        listState.scrollToItem(0, (indexForValue(selectedValue) * rowHeightPx).roundToInt())
    }

    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: 0
        }
    }

    val settledValue = if (!listState.isScrollInProgress) {
        (range.first + centeredIndex).coerceIn(range)
    } else {
        null
    }

    LaunchedEffect(settledValue) {
        settledValue?.let { value ->
            if (value != selectedValue) onValueChange(value)
        }
    }

    val snapFlingBehavior = rememberSnapFlingBehavior(
        SnapLayoutInfoProvider(lazyListState = listState, snapPosition = SnapPosition.Center)
    )

    Box(
        modifier = modifier
            .height(viewportHeight)
            .fillMaxWidth()
    ) {
        // Fixed selection pill — drawn first (bottom of the z-stack) so row text stays legible.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(rowHeight)
                .background(PrimaryTint08, RoundedCornerShape(24.dp))
                .border(1.13.dp, PrimaryTint18, RoundedCornerShape(24.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = rowHeight * paddingRows),
            modifier = Modifier.fillMaxSize()
        ) {
            items(range.count()) { i ->
                val value = range.first + i
                val isCentered = i == centeredIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0, (i * rowHeightPx).roundToInt())
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        fontSize = 22.sp,
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCentered) Cobalt else InkMuted
                    )
                }
            }
        }

        val fadeHeight = rowHeight * paddingRows
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(fadeHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(White.copy(alpha = 0.95f), White.copy(alpha = 0f))
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(fadeHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(White.copy(alpha = 0f), White.copy(alpha = 0.95f))
                    )
                )
        )
    }
}

/**
 * Modal number-wheel picker: [NumberWheel] wrapped in the "Select number" dialog chrome
 * (header, Cancel/Done). Holds its own pending value so Cancel discards without mutating
 * caller state; [onDone] reports the value chosen when the user confirms.
 */
@Composable
fun NumberWheelPickerDialog(
    range: IntRange,
    initialValue: Int,
    label: String = "SELECT NUMBER",
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit = onDismissRequest,
    onDone: (Int) -> Unit,
) {
    var pendingValue by remember(range, initialValue) {
        mutableIntStateOf(initialValue.coerceIn(range))
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = White,
            modifier = Modifier.width(280.dp)
        ) {
            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = InkMuted,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                )

                NumberWheel(
                    range = range,
                    selectedValue = pendingValue,
                    onValueChange = { pendingValue = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillButton(
                        text = "Cancel",
                        containerColor = InkFaint,
                        contentColor = InkMuted,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        onClick = onCancel
                    )
                    PillButton(
                        text = "Done",
                        containerColor = NavDark,
                        contentColor = White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        onClick = { onDone(pendingValue) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = text, fontWeight = fontWeight, fontSize = 14.sp)
        }
    }
}
