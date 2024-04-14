package my.nanihadesuka.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSide
import my.nanihadesuka.compose.foundation.VerticalScrollbarLayout
import my.nanihadesuka.compose.foundation.rememberScrollStateController

/**
 * Scrollbar for Column
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun ColumnScrollbar(
    state: ScrollState,
    modifier: Modifier = Modifier,
    rightSide: Boolean = true,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
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
    else BoxWithConstraints(modifier = modifier) {
        content()
        InternalColumnScrollbar(
            state = state,
            modifier = Modifier,
            rightSide = rightSide,
            alwaysShowScrollBar = alwaysShowScrollBar,
            thickness = thickness,
            padding = padding,
            thumbMinHeight = thumbMinHeight,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            thumbShape = thumbShape,
            visibleHeightDp = with(LocalDensity.current) { constraints.maxHeight.toDp() },
            indicatorContent = indicatorContent,
            selectionMode = selectionMode,
            selectionActionable = selectionActionable,
            hideDelayMillis = hideDelayMillis,
        )
    }
}

/**
 * Scrollbar for Column
 * Use this variation if you want to place the scrollbar independently of the Column position
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 * @param visibleHeightDp Visible height of column view
 */
@Composable
fun InternalColumnScrollbar(
    state: ScrollState,
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
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    visibleHeightDp: Dp,
) {
    val controller = rememberScrollStateController(
        state = state,
        visibleLengthDp = visibleHeightDp,
        thumbMinLength = thumbMinHeight,
        alwaysShowScrollBar = alwaysShowScrollBar,
        selectionMode = selectionMode,
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val maxHeightFloat = constraints.maxHeight.toFloat()
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
                side = if (rightSide) ScrollbarLayoutSide.End else ScrollbarLayoutSide.Start,
                selectionActionable = selectionActionable,
            ),
            indicator = indicatorContent?.let {
                { it(controller.indicatorNormalizedOffset(), controller.isSelected.value) }
            },
            draggableModifier = Modifier.draggable(
                state = rememberDraggableState { delta ->
                    controller.onDragState(delta, maxHeightFloat)
                },
                orientation = Orientation.Vertical,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offset ->
                    controller.onDragStarted(offset.y, maxHeightFloat)
                },
                onDragStopped = {
                    controller.onDragStopped()
                }
            )
        )
    }
}
