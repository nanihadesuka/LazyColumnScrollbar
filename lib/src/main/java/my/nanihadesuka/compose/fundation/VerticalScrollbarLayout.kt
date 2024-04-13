package my.nanihadesuka.compose.fundation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import my.nanihadesuka.compose.ScrollbarSelectionActionable
import my.nanihadesuka.compose.TestTagsScrollbar

@Composable
internal fun VerticalScrollbarLayout(
    scrollbarSizeNormalized: Float,
    normalizedOffset: Float,
    settings: ScrollbarLayoutSettings,
    isInAction: Boolean,
    modifier: Modifier = Modifier,
    draggableModifier: Modifier,
    indicator: (@Composable () -> Unit)?,
) {
    val isInActionSelectable = remember { mutableStateOf(isInAction) }
    LaunchedEffect(isInAction) {
        if (isInAction) {
            isInActionSelectable.value = true
        } else {
            delay(timeMillis = settings.durationAnimationMillis.toLong() + settings.hideDelayMillis.toLong())
            isInActionSelectable.value = false
        }
    }

    val activeDraggableModifier = when (settings.selectionActionable) {
        ScrollbarSelectionActionable.Always -> true
        ScrollbarSelectionActionable.WhenVisible -> isInActionSelectable.value
    }

    val hideAlpha by animateFloatAsState(
        targetValue = if (isInAction) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isInAction) 75 else settings.durationAnimationMillis,
            delayMillis = if (isInAction) 0 else settings.hideDelayMillis
        ),
        label = "scrollbar alpha value"
    )

    val hideDisplacement by animateDpAsState(
        targetValue = if (isInAction) 0.dp else 14.dp,
        animationSpec = tween(
            durationMillis = if (isInAction) 75 else settings.durationAnimationMillis,
            delayMillis = if (isInAction) 0 else settings.hideDelayMillis
        ),
        label = "scrollbar displacement value"
    )

    BoxWithConstraints(modifier) {
        Layout(
            content = {
                Box(
                    modifier = Modifier
                        .sizeIn(
                            minHeight = maxHeight * scrollbarSizeNormalized,
                            maxHeight = maxHeight * scrollbarSizeNormalized
                        )
                        .padding(
                            start = if (settings.side == ScrollbarLayoutSide.Start) settings.scrollbarPadding else 0.dp,
                            end = if (settings.side == ScrollbarLayoutSide.End) settings.scrollbarPadding else 0.dp,
                        )
                        .alpha(hideAlpha)
                        .clip(settings.thumbShape)
                        .width(settings.thumbThickness)
                        .background(settings.thumbColor)
                        .testTag(TestTagsScrollbar.scrollbar)
                )
                when (indicator) {
                    null -> Box(Modifier)
                    else -> Box(
                        Modifier
                            .testTag(TestTagsScrollbar.scrollbarIndicator)
                            .alpha(hideAlpha)
                    ) {
                        indicator()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(settings.scrollbarPadding * 2 + settings.thumbThickness)
                        .run { if (activeDraggableModifier) then(draggableModifier) else this }
                        .testTag(TestTagsScrollbar.scrollbarContainer)
                )
            },
            measurePolicy = { measurables, constraints ->
                val placeables = measurables.map { it.measure(constraints) }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val placeableThumb = placeables[0]
                    val placeableIndicator = placeables[1]
                    val placeableScrollbarArea = placeables[2]

                    val offset = (constraints.maxHeight.toFloat() * normalizedOffset).toInt()

                    val hideDispPx = when (settings.side) {
                        ScrollbarLayoutSide.Start -> -hideDisplacement.roundToPx()
                        ScrollbarLayoutSide.End -> +hideDisplacement.roundToPx()
                    }

                    placeableThumb.placeRelative(
                        x = when (settings.side) {
                            ScrollbarLayoutSide.Start -> 0
                            ScrollbarLayoutSide.End -> constraints.maxWidth - placeableThumb.width
                        } + hideDispPx,
                        y = offset
                    )

                    placeableIndicator.placeRelative(
                        x = when (settings.side) {
                            ScrollbarLayoutSide.Start -> 0 + placeableThumb.width
                            ScrollbarLayoutSide.End -> constraints.maxWidth - placeableThumb.width - placeableIndicator.width
                        } + hideDispPx,
                        y = offset + placeableThumb.height / 2 - placeableIndicator.height / 2
                    )
                    placeableScrollbarArea.placeRelative(
                        x = when (settings.side) {
                            ScrollbarLayoutSide.Start -> 0
                            ScrollbarLayoutSide.End -> constraints.maxWidth - placeableScrollbarArea.width
                        } + hideDispPx,
                        y = 0
                    )

                }
            }
        )
    }
}


@Preview(widthDp = 320, heightDp = 600)
@Composable
private fun LayoutPreview() {
    VerticalScrollbarLayout(
        scrollbarSizeNormalized = 0.2f,
        normalizedOffset = 0.4f,
        settings = ScrollbarLayoutSettings(
            durationAnimationMillis = 500,
            hideDelayMillis = 400,
            scrollbarPadding = 8.dp,
            thumbShape = CircleShape,
            thumbThickness = 6.dp,
            thumbColor = Color.Red,
            side = ScrollbarLayoutSide.Start,
            selectionActionable = ScrollbarSelectionActionable.Always
        ),
        modifier = Modifier,
        draggableModifier = Modifier,
        isInAction = true,
        indicator = {
            Text(
                text = "geregte",
                modifier = Modifier
                    .background(Color.White)
                    .padding(14.dp)
            )
        },
    )
}
