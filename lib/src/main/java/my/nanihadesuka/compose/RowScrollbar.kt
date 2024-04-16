package my.nanihadesuka.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.foundation.HorizontalScrollbarLayout
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.foundation.rememberScrollStateController

/**
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 */
@Composable
fun RowScrollbar(
    state: ScrollState,
    modifier: Modifier = Modifier,
    side: ScrollbarLayoutSide = ScrollbarLayoutSide.End,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinLength: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    enabled: Boolean = true,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!enabled) content()
    else BoxWithConstraints(
        modifier = modifier
    ) {
        content()
        InternalRowScrollbar(
            state = state,
            modifier = Modifier,
            side = side,
            alwaysShowScrollBar = alwaysShowScrollBar,
            thickness = thickness,
            padding = padding,
            thumbMinLength = thumbMinLength,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            thumbShape = thumbShape,
            visibleLengthDp = with(LocalDensity.current) { constraints.maxWidth.toDp() },
            indicatorContent = indicatorContent,
            selectionMode = selectionMode,
            selectionActionable = selectionActionable,
            hideDelayMillis = hideDelayMillis,
        )
    }
}

/**
 * Scrollbar for Row
 * Use this variation if you want to place the scrollbar independently of the Row position
 *
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 * @param visibleLengthDp Visible length of row view
 */
@Composable
fun InternalRowScrollbar(
    state: ScrollState,
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
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    visibleLengthDp: Dp,
) {
    val controller = rememberScrollStateController(
        state = state,
        visibleLengthDp = visibleLengthDp,
        thumbMinLength = thumbMinLength,
        alwaysShowScrollBar = alwaysShowScrollBar,
        selectionMode = selectionMode,
    )

    BoxWithConstraints(modifier) {
        val maxLengthPx = constraints.maxWidth.toFloat()
        HorizontalScrollbarLayout(
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
                { it(controller.indicatorNormalizedOffset(), controller.isSelected.value) }
            },
            draggableModifier = Modifier.draggable(
                state = rememberDraggableState { deltaPx ->
                    controller.onDragState(deltaPixels = deltaPx, maxLengthPx = maxLengthPx)
                },
                orientation = Orientation.Horizontal,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offsetPx ->
                    controller.onDragStarted(offsetPixels = offsetPx.x, maxLengthPixels = maxLengthPx)
                },
                onDragStopped = {
                    controller.onDragStopped()
                }
            )
        )
    }
}
