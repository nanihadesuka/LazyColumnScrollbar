package my.nanihadesuka.compose.controller

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
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
internal fun rememberLazyGridStateController(
    state: LazyGridState,
    thumbMinLength: Float,
    alwaysShowScrollBar: Boolean,
    selectionMode: ScrollbarSelectionMode,
    orientation: Orientation
): LazyGridStateController {
    val coroutineScope = rememberCoroutineScope()

    val thumbMinLengthUpdated = rememberUpdatedState(thumbMinLength)
    val alwaysShowScrollBarUpdated = rememberUpdatedState(alwaysShowScrollBar)
    val selectionModeUpdated = rememberUpdatedState(selectionMode)
    val orientationUpdated = rememberUpdatedState(orientation)
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

    // Workaround to know indirectly how many columns are being used (LazyGridState doesn't store it)
    val nColumns = remember {
        derivedStateOf {
            var count = 0
            for (item in state.layoutInfo.visibleItemsInfo) {
                if (item.column == -1)
                    break
                if (count == item.column) {
                    count += 1
                } else {
                    break
                }
            }
            count.coerceAtLeast(1)
        }
    }

    val isStickyHeaderInAction = remember {
        derivedStateOf {
            val realIndex = realFirstVisibleItem.value?.index ?: return@derivedStateOf false
            val firstVisibleIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                ?: return@derivedStateOf false
            realIndex != firstVisibleIndex
        }
    }

    fun LazyGridItemInfo.fractionHiddenTop(firstItemOffset: Int): Float {
        return when (orientationUpdated.value) {
            Orientation.Vertical -> if (size.height == 0) 0f else firstItemOffset / size.width.toFloat()
            Orientation.Horizontal -> if (size.width == 0) 0f else firstItemOffset / size.width.toFloat()
        }
    }

    fun LazyGridItemInfo.fractionVisibleBottom(viewportEndOffset: Int): Float {
        return when (orientationUpdated.value) {
            Orientation.Vertical -> if (size.height == 0) 0f else (viewportEndOffset - offset.y).toFloat() / size.height.toFloat()
            Orientation.Horizontal -> if (size.width == 0) 0f else (viewportEndOffset - offset.x).toFloat() / size.width.toFloat()
        }
    }


    val normalizedThumbSizeReal = remember {
        derivedStateOf {
            state.layoutInfo.let {
                if (it.totalItemsCount == 0)
                    return@let 0f

                val firstItem = realFirstVisibleItem.value ?: return@let 0f
                val firstPartial =
                    firstItem.fractionHiddenTop(state.firstVisibleItemScrollOffset)
                val lastPartial =
                    1f - it.visibleItemsInfo.last().fractionVisibleBottom(it.viewportEndOffset)

                val realSize =
                    (it.visibleItemsInfo.size / nColumns.value) - if (isStickyHeaderInAction.value) 1 else 0
                val realVisibleSize = realSize.toFloat() - firstPartial - lastPartial
                realVisibleSize / (it.totalItemsCount / nColumns.value).toFloat()
            }
        }
    }

    val normalizedThumbSize = remember {
        derivedStateOf {
            normalizedThumbSizeReal.value.coerceAtLeast(thumbMinLengthUpdated.value)
        }
    }

    fun offsetCorrection(top: Float): Float {
        val topRealMax = (1f - normalizedThumbSizeReal.value).coerceIn(0f, 1f)
        if (normalizedThumbSizeReal.value >= thumbMinLengthUpdated.value) {
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

    val normalizedOffsetPosition = remember {
        derivedStateOf {
            state.layoutInfo.let {
                if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty())
                    return@let 0f

                val firstItem = realFirstVisibleItem.value ?: return@let 0f
                val top = firstItem.run {
                    (index / nColumns.value).toFloat() + fractionHiddenTop(state.firstVisibleItemScrollOffset)
                } / (it.totalItemsCount / nColumns.value).toFloat()
                offsetCorrection(top)
            }
        }
    }

    val thumbIsInAction = remember {
        derivedStateOf {
            state.isScrollInProgress || isSelected.value || alwaysShowScrollBarUpdated.value
        }
    }

    return remember {
        LazyGridStateController(
            normalizedThumbSize = normalizedThumbSize,
            normalizedOffsetPosition = normalizedOffsetPosition,
            thumbIsInAction = thumbIsInAction,
            _isSelected = isSelected,
            dragOffset = dragOffset,
            selectionMode = selectionModeUpdated,
            realFirstVisibleItem = realFirstVisibleItem,
            thumbMinLength = thumbMinLengthUpdated,
            normalizedThumbSizeReal = normalizedThumbSizeReal,
            reverseLayout = reverseLayout,
            orientation = orientationUpdated,
            nColumns = nColumns,
            state = state,
            coroutineScope = coroutineScope
        )
    }
}

internal class LazyGridStateController(
    override val normalizedThumbSize: State<Float>,
    override val normalizedOffsetPosition: State<Float>,
    override val thumbIsInAction: State<Boolean>,
    private val _isSelected: MutableState<Boolean>,
    private val dragOffset: MutableFloatState,
    private val selectionMode: State<ScrollbarSelectionMode>,
    private val realFirstVisibleItem: State<LazyGridItemInfo?>,
    private val normalizedThumbSizeReal: State<Float>,
    private val thumbMinLength: State<Float>,
    private val reverseLayout: State<Boolean>,
    private val orientation: State<Orientation>,
    private val nColumns: State<Int>,
    private val state: LazyGridState,
    private val coroutineScope: CoroutineScope,
) : StateController<Int> {

    override val isSelected = _isSelected

    override fun indicatorValue(): Int {
        return state.firstVisibleItemIndex
    }

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
            reverseLayout.value -> 1f - normalizedOffsetPosition.value - normalizedThumbSize.value
            else -> normalizedOffsetPosition.value
        }

        when (selectionMode.value) {
            ScrollbarSelectionMode.Full -> {
                if (newOffset in currentOffset..(currentOffset + normalizedThumbSize.value))
                    setDragOffset(currentOffset)
                else
                    setScrollOffset(newOffset)
                _isSelected.value = true
            }

            ScrollbarSelectionMode.Thumb -> {
                if (newOffset in currentOffset..(currentOffset + normalizedThumbSize.value)) {
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

    private fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val totalItemsCount = state.layoutInfo.totalItemsCount.toFloat() / nColumns.value.toFloat()
        val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffset.floatValue)
        val index: Int = floor(exactIndex).toInt() * nColumns.value
        val remainder: Float = exactIndex - floor(exactIndex)

        coroutineScope.launch {
            state.scrollToItem(index = index, scrollOffset = 0)
            val offset = realFirstVisibleItem.value
                ?.size
                ?.let {
                    val size = when (orientation.value) {
                        Orientation.Vertical -> it.height
                        Orientation.Horizontal -> it.width
                    }
                    size.toFloat() * remainder
                }
                ?.toInt() ?: 0
            state.scrollToItem(index = index, scrollOffset = offset)
        }
    }

    private fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize.value).coerceAtLeast(0f)
        dragOffset.floatValue = value.coerceIn(0f, maxValue)
    }

    private fun offsetCorrectionInverse(top: Float): Float {
        if (normalizedThumbSizeReal.value >= thumbMinLength.value)
            return top
        val topRealMax = 1f - normalizedThumbSizeReal.value
        val topMax = 1f - thumbMinLength.value
        return top * topRealMax / topMax
    }
}