package my.nanihadesuka.compose.controller

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

@Stable
interface StateController<IndicatorValue> {
    val normalizedThumbSize: State<Float>
    val normalizedOffsetPosition: State<Float>
    val thumbIsInAction: State<Boolean>
    val isSelected: State<Boolean>

    fun indicatorValue(): IndicatorValue
    fun onDraggableState(deltaPixels: Float, maxLengthPixels: Float)
    fun onDragStarted(offsetPixels: Float, maxLengthPixels: Float)
    fun onDragStopped()
}
