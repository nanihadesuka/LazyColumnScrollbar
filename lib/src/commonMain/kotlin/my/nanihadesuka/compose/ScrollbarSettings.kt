package my.nanihadesuka.compose

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 */
@Stable
data class ScrollbarSettings(
    var enabled: Boolean = Default.enabled,
    var side: ScrollbarLayoutSide = Default.side,
    var alwaysShowScrollbar: Boolean = Default.alwaysShowScrollbar,
    var scrollbarPadding: Dp = Default.scrollbarPadding,
    var thumbThickness: Dp = Default.thumbThickness,
    var thumbShape: Shape = Default.thumbShape,
    var thumbMinLength: Float = Default.thumbMinLength,
    var thumbMaxLength: Float = Default.thumbMaxLength,
    var thumbUnselectedColor: Color = Default.thumbUnselectedColor,
    var thumbSelectedColor: Color = Default.thumbSelectedColor,
    var selectionMode: ScrollbarSelectionMode = Default.selectionMode,
    var selectionActionable: ScrollbarSelectionActionable = Default.selectionActionable,
    var hideDelayMillis: Int = Default.hideDelayMillis,
    var hideDisplacement: Dp = Default.hideDisplacement,
    var hideEasingAnimation: Easing = Default.hideEasingAnimation,
    var durationAnimationMillis: Int = Default.durationAnimationMillis,
) {
    init {
        require(thumbMinLength <= thumbMaxLength) {
            "thumbMinLength ($thumbMinLength) must be less or equal to thumbMaxLength ($thumbMaxLength)"
        }
    }

    companion object {
        val Default = ScrollbarSettings(
            enabled = true,
            side = ScrollbarLayoutSide.End,
            alwaysShowScrollbar = false,
            thumbThickness = 6.dp,
            scrollbarPadding = 8.dp,
            thumbMinLength = 0.1f,
            thumbMaxLength = 1.0f,
            thumbUnselectedColor = Color(0xFF2A59B6),
            thumbSelectedColor = Color(0xFF5281CA),
            thumbShape = CircleShape,
            selectionMode = ScrollbarSelectionMode.Thumb,
            selectionActionable = ScrollbarSelectionActionable.Always,
            hideDelayMillis = 400,
            hideDisplacement = 14.dp,
            hideEasingAnimation = FastOutSlowInEasing,
            durationAnimationMillis = 500,
        )
    }
}
