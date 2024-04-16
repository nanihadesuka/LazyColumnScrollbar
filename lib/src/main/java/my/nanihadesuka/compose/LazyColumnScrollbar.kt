package my.nanihadesuka.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.foundation.VerticalScrollbarLayout
import my.nanihadesuka.compose.foundation.rememberLazyListStateController

/**
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 */
@Composable
fun LazyColumnScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    side: ScrollbarLayoutSide = ScrollbarLayoutSide.End,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinLength: Float = 0.1f,
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
        InternalLazyColumnScrollbar(
            state = listState,
            modifier = Modifier,
            side = side,
            alwaysShowScrollBar = alwaysShowScrollBar,
            thickness = thickness,
            padding = padding,
            thumbMinLength = thumbMinLength,
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
 * Scrollbar for LazyColumn
 * Use this variation if you want to place the scrollbar independently of the LazyColumn position
 *
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 */
@Composable
fun InternalLazyColumnScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    side: ScrollbarLayoutSide = ScrollbarLayoutSide.End,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinLength: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
) {
    val controller = rememberLazyListStateController(
        state = state,
        thumbMinLength = thumbMinLength,
        alwaysShowScrollBar = alwaysShowScrollBar,
        selectionMode = selectionMode
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxLengthFloat = constraints.maxHeight.toFloat()
        VerticalScrollbarLayout(
            thumbSizeNormalized = controller.normalizedThumbSize.value,
            thumbOffsetNormalized = controller.normalizedOffsetPosition.value,
            thumbIsInAction = controller.thumbIsInAction.value,
            settings = ScrollbarLayoutSettings(
                durationAnimationMillis = 500,
                hideDelayMillis = hideDelayMillis,
                scrollbarPadding = padding,
                thumbShape = thumbShape,
                thumbThickness = thickness,
                thumbColor = if (controller.isSelected.value) thumbSelectedColor else thumbColor,
                side = side,
                selectionActionable = selectionActionable,
            ),
            indicator = indicatorContent?.let {
                { it(controller.indicatorIndex(), controller.isSelected.value) }
            },
            draggableModifier = Modifier.draggable(
                state = rememberDraggableState { delta ->
                    controller.onDraggableState(delta = delta, maxLength = maxLengthFloat)
                },
                orientation = Orientation.Vertical,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = onDragStarted@{ offset ->
                    controller.onDragStarted(offsetPixels = offset.y, maxLength = maxLengthFloat)
                },
                onDragStopped = {
                    controller.onDragStopped()
                }
            )
        )
    }
}
