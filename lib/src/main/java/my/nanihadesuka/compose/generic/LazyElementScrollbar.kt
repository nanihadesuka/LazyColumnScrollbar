package my.nanihadesuka.compose.generic

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSelectionActionable
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.controller.rememberLazyListStateController
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings

@Composable
internal fun LazyElementScrollbar(
    orientation: Orientation,
    state: LazyListState,
    modifier: Modifier,
    side: ScrollbarLayoutSide,
    alwaysShowScrollBar: Boolean,
    thickness: Dp,
    padding: Dp,
    thumbMinLength: Float,
    thumbColor: Color,
    thumbSelectedColor: Color,
    thumbShape: Shape,
    selectionMode: ScrollbarSelectionMode,
    selectionActionable: ScrollbarSelectionActionable,
    hideDelayMillis: Int,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)?,
) {
    val controller = rememberLazyListStateController(
        state = state,
        thumbMinLength = thumbMinLength,
        alwaysShowScrollBar = alwaysShowScrollBar,
        selectionMode = selectionMode
    )

    BoxWithConstraints(modifier = modifier) {
        val maxLengthPixel = when (orientation) {
            Orientation.Vertical -> constraints.maxHeight
            Orientation.Horizontal -> constraints.maxWidth
        }.toFloat()
        ScrollbarLayout(
            orientation = orientation,
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
                state = rememberDraggableState { deltaPixels ->
                    controller.onDraggableState(delta = deltaPixels, maxLength = maxLengthPixel)
                },
                orientation = orientation,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offsetPixel ->
                    controller.onDragStarted(
                        offsetPixels = when (orientation) {
                            Orientation.Horizontal -> offsetPixel.x
                            Orientation.Vertical -> offsetPixel.y
                        }, maxLength = maxLengthPixel
                    )
                },
                onDragStopped = {
                    controller.onDragStopped()
                }
            )
        )
    }
}