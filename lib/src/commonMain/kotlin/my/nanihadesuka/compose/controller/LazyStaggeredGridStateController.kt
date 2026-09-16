package my.nanihadesuka.compose.controller

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.ScrollbarSelectionMode
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Scroll position measured in items.
 *
 * @param hiddenItems items scrolled past the content start, fractions included
 * @param visibleItems items inside the content window, fractions included
 * @param contentLengthPx main axis length of the content window
 */
internal class StaggeredGridScrollMetrics(
    val hiddenItems: Float,
    val visibleItems: Float,
    val contentLengthPx: Int,
)

@Composable
internal fun rememberLazyStaggeredGridStateController(
    state: LazyStaggeredGridState,
    reverseLayout: Boolean,
    thumbMinLength: Float,
    thumbMaxLength: Float,
    alwaysShowScrollBar: Boolean,
    selectionMode: ScrollbarSelectionMode,
    orientation: Orientation
): LazyStaggeredGridStateController {
    val coroutineScope = rememberCoroutineScope()

    val thumbMinLengthUpdated = rememberUpdatedState(thumbMinLength)
    val thumbMaxLengthUpdated = rememberUpdatedState(thumbMaxLength)
    val alwaysShowScrollBarUpdated = rememberUpdatedState(alwaysShowScrollBar)
    val selectionModeUpdated = rememberUpdatedState(selectionMode)
    val orientationUpdated = rememberUpdatedState(orientation)
    val reverseLayoutUpdated = rememberUpdatedState(reverseLayout)

    val isSelected = remember { mutableStateOf(false) }
    val dragOffset = remember { mutableFloatStateOf(0f) }

    fun LazyStaggeredGridItemInfo.mainAxisOffset() = when (orientationUpdated.value) {
        Orientation.Vertical -> offset.y
        Orientation.Horizontal -> offset.x
    }

    fun LazyStaggeredGridItemInfo.mainAxisSize() = when (orientationUpdated.value) {
        Orientation.Vertical -> size.height
        Orientation.Horizontal -> size.width
    }

    // Item start offsets never decrease with the item index, so any item that is not laid out
    // and whose index lies between the smallest and largest visible index is fully scrolled past.
    // Summing per-item fractions gives values that are continuous and monotonic while scrolling,
    // independent of which lane holds the first visible item.
    val scrollMetrics = remember {
        derivedStateOf {
            val info = state.layoutInfo
            val items = info.visibleItemsInfo
            if (info.totalItemsCount == 0 || items.isEmpty())
                return@derivedStateOf StaggeredGridScrollMetrics(0f, 0f, 0)

            val contentEnd = info.viewportEndOffset - info.afterContentPadding
            var minIndex = Int.MAX_VALUE
            var maxIndex = Int.MIN_VALUE
            var hidden = 0f
            var visible = 0f
            for (item in items) {
                minIndex = min(minIndex, item.index)
                maxIndex = max(maxIndex, item.index)
                val start = item.mainAxisOffset()
                val size = item.mainAxisSize()
                if (size <= 0) {
                    if (start < 0) hidden += 1f
                    continue
                }
                val end = start + size
                hidden += (-start.toFloat() / size).coerceIn(0f, 1f)
                visible += ((min(end, contentEnd) - max(start, 0)).toFloat() / size).coerceIn(0f, 1f)
            }
            val notLaidOutBetween = (maxIndex - minIndex + 1) - items.size
            StaggeredGridScrollMetrics(
                hiddenItems = hidden + minIndex + notLaidOutBetween,
                visibleItems = visible,
                contentLengthPx = contentEnd,
            )
        }
    }

    val thumbSizeNormalizedReal = remember {
        derivedStateOf {
            val total = state.layoutInfo.totalItemsCount
            if (total == 0) 0f else scrollMetrics.value.visibleItems / total
        }
    }

    val thumbSizeNormalized = remember {
        derivedStateOf {
            thumbSizeNormalizedReal.value.coerceIn(
                thumbMinLengthUpdated.value,
                thumbMaxLengthUpdated.value,
            )
        }
    }

    fun offsetCorrection(top: Float): Float {
        val topRealMax = (1f - thumbSizeNormalizedReal.value).coerceIn(0f, 1f)
        if (thumbSizeNormalizedReal.value >= thumbMinLengthUpdated.value) {
            return when {
                reverseLayoutUpdated.value -> topRealMax - top
                else -> top
            }
        }

        val topMax = 1f - thumbMinLengthUpdated.value
        return when {
            reverseLayoutUpdated.value -> (topRealMax - top) * topMax / topRealMax
            else -> top * topMax / topRealMax
        }
    }

    val thumbOffsetNormalized = remember {
        derivedStateOf {
            val total = state.layoutInfo.totalItemsCount
            if (total == 0 || state.layoutInfo.visibleItemsInfo.isEmpty()) 0f
            else offsetCorrection(scrollMetrics.value.hiddenItems / total)
        }
    }

    val thumbIsInAction = remember {
        derivedStateOf {
            state.isScrollInProgress || isSelected.value || alwaysShowScrollBarUpdated.value
        }
    }

    return remember {
        LazyStaggeredGridStateController(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbSizeNormalizedReal = thumbSizeNormalizedReal,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            _isSelected = isSelected,
            dragOffset = dragOffset,
            selectionMode = selectionModeUpdated,
            scrollMetrics = scrollMetrics,
            thumbMinLength = thumbMinLengthUpdated,
            reverseLayout = reverseLayoutUpdated,
            state = state,
            coroutineScope = coroutineScope
        )
    }
}

internal class LazyStaggeredGridStateController(
    override val thumbSizeNormalized: State<Float>,
    override val thumbOffsetNormalized: State<Float>,
    override val thumbIsInAction: State<Boolean>,
    private val _isSelected: MutableState<Boolean>,
    private val dragOffset: MutableFloatState,
    private val selectionMode: State<ScrollbarSelectionMode>,
    private val scrollMetrics: State<StaggeredGridScrollMetrics>,
    private val thumbSizeNormalizedReal: State<Float>,
    private val thumbMinLength: State<Float>,
    private val reverseLayout: State<Boolean>,
    private val state: LazyStaggeredGridState,
    private val coroutineScope: CoroutineScope,
) : StateController<Int> {

    override val isSelected = _isSelected

    override fun indicatorValue(): Int {
        return state.firstVisibleItemIndex
    }

    override fun onDraggableState(deltaPixels: Float, maxLengthPixels: Float) {
        val displace = if (reverseLayout.value) -deltaPixels else deltaPixels
        if (isSelected.value) {
            setScrollOffset(dragOffset.floatValue + displace / maxLengthPixels, direction = displace)
        }
    }

    override fun onDragStarted(offsetPixels: Float, maxLengthPixels: Float) {
        if (maxLengthPixels <= 0f) return
        val newOffset = when {
            reverseLayout.value -> (maxLengthPixels - offsetPixels) / maxLengthPixels
            else -> offsetPixels / maxLengthPixels
        }
        val currentOffset = when {
            reverseLayout.value -> 1f - thumbOffsetNormalized.value - thumbSizeNormalized.value
            else -> thumbOffsetNormalized.value
        }

        when (selectionMode.value) {
            ScrollbarSelectionMode.Full -> {
                if (newOffset in currentOffset..(currentOffset + thumbSizeNormalized.value))
                    setDragOffset(currentOffset)
                else
                    setScrollOffset(newOffset, direction = 0f)
                _isSelected.value = true
            }

            ScrollbarSelectionMode.Thumb -> {
                if (newOffset in currentOffset..(currentOffset + thumbSizeNormalized.value)) {
                    setDragOffset(currentOffset)
                    _isSelected.value = true
                }
            }

            ScrollbarSelectionMode.Disabled -> Unit
        }
    }

    override fun onDragStopped() {
        _isSelected.value = false
    }

    /**
     * @param direction sign of the drag movement (positive toward the list end), or 0 for a jump.
     */
    private fun setScrollOffset(newOffset: Float, direction: Float) {
        setDragOffset(newOffset)
        val targetOffset = dragOffset.floatValue

        coroutineScope.launch {
            val totalItems = state.layoutInfo.totalItemsCount
            if (totalItems == 0) return@launch
            val targetHiddenItems = totalItems * offsetCorrectionInverse(targetOffset)

            if (direction == 0f) {
                state.scrollToItem(floor(targetHiddenItems).toInt().coerceIn(0, totalItems - 1))
            }

            // Item sizes outside the viewport are unknown, so scroll by pixels using the item
            // density of the visible window; each drag event re-targets from the new layout.
            val metrics = scrollMetrics.value
            if (metrics.contentLengthPx <= 0 || metrics.visibleItems <= 0f) return@launch
            val itemsPerPixel = metrics.visibleItems / metrics.contentLengthPx
            val deltaPixels = (targetHiddenItems - metrics.hiddenItems) / itemsPerPixel

            // Never scroll against the drag direction, so the content can't oscillate.
            if (direction > 0f && deltaPixels <= 0f) return@launch
            if (direction < 0f && deltaPixels >= 0f) return@launch
            state.scrollBy(deltaPixels)
        }
    }

    private fun setDragOffset(value: Float) {
        val maxValue = (1f - thumbSizeNormalized.value).coerceAtLeast(0f)
        dragOffset.floatValue = value.coerceIn(0f, maxValue)
    }

    private fun offsetCorrectionInverse(top: Float): Float {
        if (thumbSizeNormalizedReal.value >= thumbMinLength.value)
            return top
        val topRealMax = 1f - thumbSizeNormalizedReal.value
        val topMax = 1f - thumbMinLength.value
        return top * topRealMax / topMax
    }
}
