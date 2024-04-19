package my.nanihadesuka.compose.generic

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSelectionActionable
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.controller.rememberScrollStateController

@Composable
internal fun ElementScrollbar(
    orientation: Orientation,
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
                { it(controller.indicatorNormalizedOffset(), controller.isSelected.value) }
            },
            draggableModifier = Modifier.draggable(
                state = rememberDraggableState { deltaPixel ->
                    controller.onDragState(deltaPixels = deltaPixel, maxLengthPx = maxLengthPixel)
                },
                orientation = orientation,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offsetPixel ->
                    controller.onDragStarted(
                        offsetPixels = when (orientation) {
                            Orientation.Horizontal -> offsetPixel.x
                            Orientation.Vertical -> offsetPixel.y
                        },
                        maxLengthPixels = maxLengthPixel
                    )
                },
                onDragStopped = {
                    controller.onDragStopped()
                }
            )
        )
    }
}
