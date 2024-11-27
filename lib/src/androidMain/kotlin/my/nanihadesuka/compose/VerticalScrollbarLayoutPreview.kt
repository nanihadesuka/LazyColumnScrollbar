package my.nanihadesuka.compose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.foundation.VerticalScrollbarLayout

@Preview(widthDp = 320, heightDp = 600)
@Composable
private fun VerticalScrollbarLayoutPreview() {
    Box {
        VerticalScrollbarLayout(
            thumbSizeNormalized = 0.2f,
            thumbOffsetNormalized = 0.4f,
            thumbIsSelected = true,
            settings = ScrollbarLayoutSettings(
                durationAnimationMillis = 500,
                hideDelayMillis = 400,
                scrollbarPadding = 8.dp,
                thumbShape = CircleShape,
                thumbThickness = 6.dp,
                thumbUnselectedColor = Color.Green,
                thumbSelectedColor = Color.Red,
                side = ScrollbarLayoutSide.Start,
                selectionActionable = ScrollbarSelectionActionable.Always,
                hideEasingAnimation = FastOutSlowInEasing,
                hideDisplacement = 14.dp,
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