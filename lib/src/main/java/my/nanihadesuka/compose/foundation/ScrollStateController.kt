package my.nanihadesuka.compose.foundation

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.ScrollbarSelectionMode

@Composable
internal fun rememberScrollStateController(
    state: ScrollState,
    visibleLengthDp: Dp,
    thumbMinLength: Float,
    alwaysShowScrollBar: Boolean,
    selectionMode: ScrollbarSelectionMode
): ScrollStateController {
    val coroutineScope = rememberCoroutineScope()

    val isSelected = remember { mutableStateOf(false) }

    val dragOffset = remember { mutableFloatStateOf(0f) }

    val fullLengthDp = with(LocalDensity.current) { state.maxValue.toDp() + visibleLengthDp }

    val normalizedThumbSizeReal = remember(visibleLengthDp, state.maxValue) {
        derivedStateOf {
            if (fullLengthDp == 0.dp) 1f else {
                val normalizedDp = visibleLengthDp / fullLengthDp
                normalizedDp.coerceIn(0f, 1f)
            }
        }
    }

    val normalizedThumbSize = remember(normalizedThumbSizeReal.value) {
        derivedStateOf {
            normalizedThumbSizeReal.value.coerceAtLeast(thumbMinLength)
        }
    }

    val normalizedThumbSizeUpdated = rememberUpdatedState(newValue = normalizedThumbSize.value)

    fun offsetCorrection(top: Float): Float {
        val topRealMax = 1f
        val topMax = (1f - normalizedThumbSizeUpdated.value).coerceIn(0f, 1f)
        return top * topMax / topRealMax
    }

    val normalizedOffsetPosition = remember {
        derivedStateOf {
            if (state.maxValue == 0) return@derivedStateOf 0f
            val normalized = state.value.toFloat() / state.maxValue.toFloat()
            offsetCorrection(normalized)
        }
    }
    val normalizedOffsetPositionUpdated = rememberUpdatedState(normalizedOffsetPosition.value)

    val thumbIsInAction = remember(alwaysShowScrollBar) {
        derivedStateOf {
            state.isScrollInProgress || isSelected.value || alwaysShowScrollBar
        }
    }

    return remember(alwaysShowScrollBar, normalizedThumbSizeReal.value) {
        ScrollStateController(
            normalizedThumbSize = normalizedThumbSizeUpdated,
            normalizedOffsetPosition = normalizedOffsetPositionUpdated,
            thumbIsInAction = thumbIsInAction,
            _isSelected = isSelected,
            dragOffset = dragOffset,
            state = state,
            selectionMode = selectionMode,
            coroutineScope = coroutineScope
        )
    }
}

internal class ScrollStateController(
    val normalizedThumbSize: State<Float>,
    val normalizedOffsetPosition: State<Float>,
    val thumbIsInAction: State<Boolean>,
    private val _isSelected: MutableState<Boolean>,
    private val dragOffset: MutableState<Float>,
    private val selectionMode: ScrollbarSelectionMode,
    private val state: ScrollState,
    private val coroutineScope: CoroutineScope,
) {
    val isSelected: State<Boolean> = _isSelected

    fun onDragStarted(offsetPixels: Float, maxLengthPixels: Float) {
        val newOffset = offsetPixels / maxLengthPixels
        val currentOffset = normalizedOffsetPosition.value

        when (selectionMode) {
            ScrollbarSelectionMode.Full -> {
                if (newOffset in currentOffset..(currentOffset + normalizedThumbSize.value))
                    setDragOffset(currentOffset)
                else
                    setScrollOffset(newOffset)
                _isSelected.value = true
            }

            ScrollbarSelectionMode.Thumb -> {
                if (newOffset in currentOffset..(currentOffset + normalizedThumbSize.value)) {
                    setDragOffset(currentOffset)
                    _isSelected.value = true
                }
            }

            ScrollbarSelectionMode.Disabled -> Unit
        }
    }

    fun onDragStopped() {
        _isSelected.value = false
    }

    fun onDragState(deltaPixels: Float, maxLengthPx: Float) {
        if (isSelected.value) {
            setScrollOffset(dragOffset.value + deltaPixels / maxLengthPx)
        }
    }

    fun indicatorNormalizedOffset(): Float {
        return offsetCorrectionInverse(normalizedOffsetPosition.value)
    }

    private fun offsetCorrectionInverse(top: Float): Float {
        val topRealMax = 1f
        val topMax = 1f - normalizedThumbSize.value
        if (topMax == 0f) return top
        return (top * topRealMax / topMax).coerceAtLeast(0f)
    }

    private fun setScrollOffset(newOffset: Float) {
        setDragOffset(newOffset)
        val exactIndex = offsetCorrectionInverse(state.maxValue * dragOffset.value).toInt()
        coroutineScope.launch {
            state.scrollTo(exactIndex)
        }
    }

    private fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize.value).coerceAtLeast(0f)
        dragOffset.value = value.coerceIn(0f, maxValue)
    }
}
