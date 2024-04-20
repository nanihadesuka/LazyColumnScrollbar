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
    alwaysShowScrollBar: Boolean,
    selectionMode: ScrollbarSelectionMode
): LazyListStateController {
    val coroutineScope = rememberCoroutineScope()

    val thumbMinLengthUpdated = rememberUpdatedState(thumbMinLength)
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

    val isStickyHeaderInAction = remember {
        derivedStateOf {
            val realIndex = realFirstVisibleItem.value?.index ?: return@derivedStateOf false
            val firstVisibleIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                ?: return@derivedStateOf false
            realIndex != firstVisibleIndex
        }
    }

    fun LazyListItemInfo.fractionHiddenTop(firstItemOffset: Int) =
        if (size == 0) 0f else firstItemOffset / size.toFloat()

    fun LazyListItemInfo.fractionVisibleBottom(viewportEndOffset: Int) =
        if (size == 0) 0f else (viewportEndOffset - offset).toFloat() / size.toFloat()

    val normalizedThumbSizeReal = remember {
        derivedStateOf {
            state.layoutInfo.let {
                if (it.totalItemsCount == 0)
                    return@let 0f

                val firstItem = realFirstVisibleItem.value ?: return@let 0f
                val firstPartial =
                    firstItem.fractionHiddenTop(state.firstVisibleItemScrollOffset)
                val lastPartial = 1f - it.visibleItemsInfo.last().fractionVisibleBottom(
                    it.viewportEndOffset - it.afterContentPadding
                )

                val realSize = it.visibleItemsInfo.size - if (isStickyHeaderInAction.value) 1 else 0
                val realVisibleSize = realSize.toFloat() - firstPartial - lastPartial
                realVisibleSize / it.totalItemsCount.toFloat()
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
            normalizedThumbSize = normalizedThumbSize,
            normalizedOffsetPosition = normalizedOffsetPosition,
            thumbIsInAction = thumbIsInAction,
            _isSelected = isSelected,
            dragOffset = dragOffset,
            selectionMode = selectionModeUpdated,
            normalizedThumbSizeReal = normalizedThumbSizeReal,
            realFirstVisibleItem = realFirstVisibleItem,
            reverseLayout = reverseLayout,
            thumbMinLength = thumbMinLengthUpdated,
            coroutineScope = coroutineScope,
            state = state,
        )
    }
}

internal class LazyListStateController(
    override val normalizedThumbSize: State<Float>,
    override val normalizedOffsetPosition: State<Float>,
    override val thumbIsInAction: State<Boolean>,
    private val _isSelected: MutableState<Boolean>,
    private val dragOffset: MutableFloatState,
    private val normalizedThumbSizeReal: State<Float>,
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

    private fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val totalItemsCount = state.layoutInfo.totalItemsCount.toFloat()
        val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffset.floatValue)
        val index: Int = floor(exactIndex).toInt()
        val remainder: Float = exactIndex - floor(exactIndex)

        coroutineScope.launch {
            state.scrollToItem(index = index, scrollOffset = 0)
            val offset = realFirstVisibleItem.value
                ?.size
                ?.let { it.toFloat() * remainder }
                ?: 0f
            state.scrollBy(offset)
        }
    }
}
