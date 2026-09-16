package my.nanihadesuka.compose.controller

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
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

@Composable
internal fun rememberLazyListStateController(
    state: LazyListState,
    thumbMinLength: Float,
    thumbMaxLength: Float,
    alwaysShowScrollBar: Boolean,
    selectionMode: ScrollbarSelectionMode
): LazyListStateController {
    val coroutineScope = rememberCoroutineScope()

    val thumbMinLengthUpdated = rememberUpdatedState(thumbMinLength)
    val thumbMaxLengthUpdated = rememberUpdatedState(thumbMaxLength)
    val alwaysShowScrollBarUpdated = rememberUpdatedState(alwaysShowScrollBar)
    val selectionModeUpdated = rememberUpdatedState(selectionMode)
    val reverseLayout = remember { derivedStateOf { state.layoutInfo.reverseLayout } }

    val isSelected = remember { mutableStateOf(false) }
    val dragOffset = remember { mutableFloatStateOf(0f) }

    val realFirstVisibleItem = remember {
        derivedStateOf {
            state.layoutInfo.visibleItemsInfo.firstOrNull {
                it.index == state.firstVisibleItemIndex
            }
        }
    }

    fun LazyListItemInfo.fractionHiddenTop(firstItemOffset: Int): Float {
        val sizeWithSpacing = size + state.layoutInfo.mainAxisItemSpacing
        return if (sizeWithSpacing <= 0) 0f
        else (firstItemOffset.toFloat() / sizeWithSpacing).coerceIn(0f, 1f)
    }

    val thumbSizeNormalizedReal = remember {
        derivedStateOf {
            state.layoutInfo.let {
                if (it.totalItemsCount == 0)
                    return@let 0f

                val firstItem = realFirstVisibleItem.value ?: return@let 0f
                val contentEnd = it.viewportEndOffset - it.afterContentPadding

                // Items laid out inside the start content padding and a pinned sticky header have
                // index < firstVisibleItemIndex; items inside the end content padding start past
                // contentEnd. Neither is part of the visible content window.
                var visibleCount = 0
                var lastItem = firstItem
                for (item in it.visibleItemsInfo) {
                    if (item.index < firstItem.index || item.offset >= contentEnd) continue
                    visibleCount++
                    if (item.index > lastItem.index) lastItem = item
                }

                val firstPartial = firstItem.fractionHiddenTop(state.firstVisibleItemScrollOffset)
                // A pinned first item reports a clamped offset, so derive its end from the scroll offset.
                val lastEnd = if (lastItem.index == firstItem.index)
                    firstItem.size - state.firstVisibleItemScrollOffset
                else
                    lastItem.offset + lastItem.size
                val lastPartial = if (lastItem.size == 0) 0f
                else ((lastEnd - contentEnd).toFloat() / lastItem.size).coerceIn(0f, 1f)

                val realVisibleSize = visibleCount - firstPartial - lastPartial
                realVisibleSize.coerceAtLeast(0f) / it.totalItemsCount.toFloat()
            }
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
                reverseLayout.value -> topRealMax - top
                else -> top
            }
        }

        val topMax = 1f - thumbMinLengthUpdated.value
        return when {
            reverseLayout.value -> (topRealMax - top) * topMax / topRealMax
            else -> top * topMax / topRealMax
        }
    }

    val thumbOffsetNormalized = remember {
        derivedStateOf {
            state.layoutInfo.let {
                if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty())
                    return@let 0f

                val firstItem = realFirstVisibleItem.value ?: return@let 0f
                val top = firstItem
                    .run { index.toFloat() + fractionHiddenTop(state.firstVisibleItemScrollOffset) } / it.totalItemsCount.toFloat()
                offsetCorrection(top)
            }
        }
    }

    val thumbIsInAction = remember {
        derivedStateOf { state.isScrollInProgress || isSelected.value || alwaysShowScrollBarUpdated.value }
    }

    return remember {
        LazyListStateController(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbSizeNormalizedReal = thumbSizeNormalizedReal,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            _isSelected = isSelected,
            dragOffset = dragOffset,
            selectionMode = selectionModeUpdated,
            realFirstVisibleItem = realFirstVisibleItem,
            reverseLayout = reverseLayout,
            thumbMinLength = thumbMinLengthUpdated,
            coroutineScope = coroutineScope,
            state = state,
        )
    }
}

internal class LazyListStateController(
    override val thumbSizeNormalized: State<Float>,
    override val thumbOffsetNormalized: State<Float>,
    override val thumbIsInAction: State<Boolean>,
    private val _isSelected: MutableState<Boolean>,
    private val dragOffset: MutableFloatState,
    private val thumbSizeNormalizedReal: State<Float>,
    private val realFirstVisibleItem: State<LazyListItemInfo?>,
    private val selectionMode: State<ScrollbarSelectionMode>,
    private val reverseLayout: State<Boolean>,
    private val thumbMinLength: State<Float>,
    private val state: LazyListState,
    private val coroutineScope: CoroutineScope,
) : StateController<Int> {
    override val isSelected: State<Boolean> = _isSelected

    private val firstVisibleItemIndex = derivedStateOf { state.firstVisibleItemIndex }

    override fun indicatorValue() = firstVisibleItemIndex.value

    override fun onDraggableState(deltaPixels: Float, maxLengthPixels: Float) {
        val displace = if (reverseLayout.value) -deltaPixels else deltaPixels // side effect ?
        if (isSelected.value) {
            setScrollOffset(dragOffset.floatValue + displace / maxLengthPixels)
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
                    setScrollOffset(newOffset)
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

    private fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val totalItemsCount = state.layoutInfo.totalItemsCount.toFloat()
        val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffset.floatValue)
        val index: Int = floor(exactIndex).toInt()
        val remainder: Float = exactIndex - floor(exactIndex)

        coroutineScope.launch {
            state.scrollToItem(index = index, scrollOffset = 0)
            val offset = realFirstVisibleItem.value
                ?.let { (it.size + state.layoutInfo.mainAxisItemSpacing).toFloat() * remainder }
                ?: 0f
            state.scrollBy(offset)
        }
    }
}
