package my.nanihadesuka.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSide
import my.nanihadesuka.compose.foundation.VerticalScrollbarLayout
import kotlin.math.floor

/**
 * @param state LazyGridState
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun LazyGridVerticalScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    rightSide: Boolean = true,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    enabled: Boolean = true,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!enabled) content()
    else Box(modifier = modifier) {
        content()
        InternalLazyGridVerticalScrollbar(
            gridState = state,
            modifier = Modifier,
            rightSide = rightSide,
            alwaysShowScrollBar = alwaysShowScrollBar,
            thickness = thickness,
            padding = padding,
            thumbMinHeight = thumbMinHeight,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            selectionActionable = selectionActionable,
            hideDelayMillis = hideDelayMillis,
            thumbShape = thumbShape,
            selectionMode = selectionMode,
            indicatorContent = indicatorContent,
        )
    }
}

/**
 * internal function
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
internal fun InternalLazyGridVerticalScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    rightSide: Boolean = true,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
) {
    val firstVisibleItemIndex = remember { derivedStateOf { gridState.firstVisibleItemIndex } }

    val coroutineScope = rememberCoroutineScope()

    var isSelected by remember { mutableStateOf(false) }

    var dragOffset by remember { mutableFloatStateOf(0f) }

    val reverseLayout by remember { derivedStateOf { gridState.layoutInfo.reverseLayout } }

    val realFirstVisibleItem by remember {
        derivedStateOf {
            gridState.layoutInfo.visibleItemsInfo.firstOrNull {
                it.index == gridState.firstVisibleItemIndex
            }
        }
    }

    // Workaround to know indirectly how many columns are being used (LazyGridState doesn't store it)
    val nColumns by remember {
        derivedStateOf {
            var count = 0
            for (item in gridState.layoutInfo.visibleItemsInfo) {
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

    val isStickyHeaderInAction by remember {
        derivedStateOf {
            val realIndex = realFirstVisibleItem?.index ?: return@derivedStateOf false
            val firstVisibleIndex = gridState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                ?: return@derivedStateOf false
            realIndex != firstVisibleIndex
        }
    }

    fun LazyGridItemInfo.fractionHiddenTop(firstItemOffset: Int) =
        if (size.height == 0) 0f else firstItemOffset / size.height.toFloat()

    fun LazyGridItemInfo.fractionVisibleBottom(viewportEndOffset: Int) =
        if (size.height == 0) 0f else (viewportEndOffset - offset.y).toFloat() / size.height.toFloat()

    val normalizedThumbSizeReal by remember {
        derivedStateOf {
            gridState.layoutInfo.let {
                if (it.totalItemsCount == 0)
                    return@let 0f

                val firstItem = realFirstVisibleItem ?: return@let 0f
                val firstPartial =
                    firstItem.fractionHiddenTop(gridState.firstVisibleItemScrollOffset)
                val lastPartial =
                    1f - it.visibleItemsInfo.last().fractionVisibleBottom(it.viewportEndOffset)

                val realSize =
                    (it.visibleItemsInfo.size / nColumns) - if (isStickyHeaderInAction) 1 else 0
                val realVisibleSize = realSize.toFloat() - firstPartial - lastPartial
                realVisibleSize / (it.totalItemsCount / nColumns).toFloat()
            }
        }
    }

    val normalizedThumbSize by remember {
        derivedStateOf {
            normalizedThumbSizeReal.coerceAtLeast(thumbMinHeight)
        }
    }

    fun offsetCorrection(top: Float): Float {
        val topRealMax = (1f - normalizedThumbSizeReal).coerceIn(0f, 1f)
        if (normalizedThumbSizeReal >= thumbMinHeight) {
            return when {
                reverseLayout -> topRealMax - top
                else -> top
            }
        }

        val topMax = 1f - thumbMinHeight
        return when {
            reverseLayout -> (topRealMax - top) * topMax / topRealMax
            else -> top * topMax / topRealMax
        }
    }

    fun offsetCorrectionInverse(top: Float): Float {
        if (normalizedThumbSizeReal >= thumbMinHeight)
            return top
        val topRealMax = 1f - normalizedThumbSizeReal
        val topMax = 1f - thumbMinHeight
        return top * topRealMax / topMax
    }

    val normalizedOffsetPosition by remember {
        derivedStateOf {
            gridState.layoutInfo.let {
                if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty())
                    return@let 0f

                val firstItem = realFirstVisibleItem ?: return@let 0f
                val top = firstItem.run {
                    (index / nColumns).toFloat() + fractionHiddenTop(gridState.firstVisibleItemScrollOffset)
                } / (it.totalItemsCount / nColumns).toFloat()
                offsetCorrection(top)
            }
        }
    }

    fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
        dragOffset = value.coerceIn(0f, maxValue)
    }

    fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val totalItemsCount = gridState.layoutInfo.totalItemsCount.toFloat() / nColumns.toFloat()
        val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffset)
        val index: Int = floor(exactIndex).toInt() * nColumns
        val remainder: Float = exactIndex - floor(exactIndex)

        coroutineScope.launch {
            gridState.scrollToItem(index = index, scrollOffset = 0)
            val offset = realFirstVisibleItem
                ?.size
                ?.let { it.height.toFloat() * remainder }
                ?.toInt() ?: 0
            gridState.scrollToItem(index = index, scrollOffset = offset)
        }
    }

    val isInAction = gridState.isScrollInProgress || isSelected || alwaysShowScrollBar

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxHeightFloat = constraints.maxHeight.toFloat()

        VerticalScrollbarLayout(
            thumbSizeNormalized = normalizedThumbSize,
            thumbOffsetNormalized = normalizedOffsetPosition,
            thumbIsInAction = isInAction,
            settings = ScrollbarLayoutSettings(
                durationAnimationMillis = 500,
                hideDelayMillis = hideDelayMillis,
                scrollbarPadding = padding,
                thumbShape = thumbShape,
                thumbThickness = thickness,
                thumbColor = if (isSelected) thumbSelectedColor else thumbColor,
                side = if (rightSide) ScrollbarLayoutSide.End else ScrollbarLayoutSide.Start,
                selectionActionable = selectionActionable
            ),
            indicator = indicatorContent?.let {
                { it(firstVisibleItemIndex.value, isSelected) }
            },
            draggableModifier = Modifier.draggable(
                state = rememberDraggableState { delta ->
                    val displace = if (reverseLayout) -delta else delta // side effect ?
                    if (isSelected) {
                        setScrollOffset(dragOffset + displace / maxHeightFloat)
                    }
                },
                orientation = Orientation.Vertical,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = onDragStarted@{ offset ->
                    if (maxHeightFloat <= 0f) return@onDragStarted
                    val newOffset = when {
                        reverseLayout -> (maxHeightFloat - offset.y) / maxHeightFloat
                        else -> offset.y / maxHeightFloat
                    }
                    val currentOffset = when {
                        reverseLayout -> 1f - normalizedOffsetPosition - normalizedThumbSize
                        else -> normalizedOffsetPosition
                    }

                    when (selectionMode) {
                        ScrollbarSelectionMode.Full -> {
                            if (newOffset in currentOffset..(currentOffset + normalizedThumbSize))
                                setDragOffset(currentOffset)
                            else
                                setScrollOffset(newOffset)
                            isSelected = true
                        }

                        ScrollbarSelectionMode.Thumb -> {
                            if (newOffset in currentOffset..(currentOffset + normalizedThumbSize)) {
                                setDragOffset(currentOffset)
                                isSelected = true
                            }
                        }

                        ScrollbarSelectionMode.Disabled -> Unit
                    }
                },
                onDragStopped = {
                    isSelected = false
                }
            )
        )
    }
}
