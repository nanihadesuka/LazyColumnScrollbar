package my.nanihadesuka.compose.foundation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSelectionActionable

@Stable
internal data class ScrollbarLayoutSettings(
    val durationAnimationMillis: Int,
    val hideDelayMillis: Int,
    val scrollbarPadding: Dp,
    val thumbShape: Shape,
    val thumbThickness: Dp,
    val thumbUnselectedColor: Color,
    val thumbSelectedColor: Color,
    val side: ScrollbarLayoutSide,
    val selectionActionable: ScrollbarSelectionActionable,
)