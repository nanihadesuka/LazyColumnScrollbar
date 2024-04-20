package my.nanihadesuka.compose.foundation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSelectionActionable
import my.nanihadesuka.compose.TestTagsScrollbar

@Composable
internal fun HorizontalScrollbarLayout(
    thumbSizeNormalized: Float,
    thumbOffsetNormalized: Float,
    thumbIsInAction: Boolean,
    settings: ScrollbarLayoutSettings,
    draggableModifier: Modifier,
    indicator: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isInActionSelectable = remember { mutableStateOf(thumbIsInAction) }
    LaunchedEffect(thumbIsInAction) {
        if (thumbIsInAction) {
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
        targetValue = if (thumbIsInAction) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (thumbIsInAction) 75 else settings.durationAnimationMillis,
            delayMillis = if (thumbIsInAction) 0 else settings.hideDelayMillis
        ),
        label = "scrollbar alpha value"
    )

    val hideDisplacement by animateDpAsState(
        targetValue = if (thumbIsInAction) 0.dp else 14.dp,
        animationSpec = tween(
            durationMillis = if (thumbIsInAction) 75 else settings.durationAnimationMillis,
            delayMillis = if (thumbIsInAction) 0 else settings.hideDelayMillis
        ),
        label = "scrollbar displacement value"
    )

    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(thumbSizeNormalized)
                    .padding(
                        top = if (settings.side == ScrollbarLayoutSide.Start) settings.scrollbarPadding else 0.dp,
                        bottom = if (settings.side == ScrollbarLayoutSide.End) settings.scrollbarPadding else 0.dp,
                    )
                    .alpha(hideAlpha)
                    .clip(settings.thumbShape)
                    .height(settings.thumbThickness)
                    .background(settings.thumbColor)
                    .testTag(TestTagsScrollbar.scrollbarThumb)
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
                    .fillMaxWidth()
                    .height(settings.scrollbarPadding * 2 + settings.thumbThickness)
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

                val offset = (constraints.maxWidth.toFloat() * thumbOffsetNormalized).toInt()

                val hideDisplacementPx = when (settings.side) {
                    ScrollbarLayoutSide.Start -> -hideDisplacement.roundToPx()
                    ScrollbarLayoutSide.End -> +hideDisplacement.roundToPx()
                }

                placeableThumb.placeRelative(
                    y = when (settings.side) {
                        ScrollbarLayoutSide.Start -> 0
                        ScrollbarLayoutSide.End -> constraints.maxHeight - placeableThumb.height
                    } + hideDisplacementPx,
                    x = offset
                )
                placeableIndicator.placeRelative(
                    y = when (settings.side) {
                        ScrollbarLayoutSide.Start -> 0 + placeableThumb.height
                        ScrollbarLayoutSide.End -> constraints.maxHeight - placeableThumb.height - placeableIndicator.height
                    } + hideDisplacementPx,
                    x = offset + placeableThumb.width / 2 - placeableIndicator.width / 2
                )
                placeableScrollbarArea.placeRelative(
                    y = when (settings.side) {
                        ScrollbarLayoutSide.Start -> 0
                        ScrollbarLayoutSide.End -> constraints.maxHeight - placeableScrollbarArea.height
                    },
                    x = 0
                )
            }
        }
    )
}


@Preview(widthDp = 320, heightDp = 600)
@Composable
private fun LayoutPreview() {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .padding(bottom = 30.dp)
    ) {
        HorizontalScrollbarLayout(
            modifier = Modifier
                .border(1.dp, Color.Green),
            thumbSizeNormalized = 0.2f,
            thumbOffsetNormalized = 0.4f,
            settings = ScrollbarLayoutSettings(
                durationAnimationMillis = 500,
                hideDelayMillis = 400,
                scrollbarPadding = 8.dp,
                thumbShape = CircleShape,
                thumbThickness = 6.dp,
                thumbColor = Color.Red,
                side = ScrollbarLayoutSide.End,
                selectionActionable = ScrollbarSelectionActionable.Always
            ),
            draggableModifier = Modifier,
            thumbIsInAction = true,
            indicator = {
                Text(
                    text = "I'm groot",
                    modifier = Modifier
                        .background(Color.White)
                        .padding(14.dp)
                )
            },
        )
    }
}