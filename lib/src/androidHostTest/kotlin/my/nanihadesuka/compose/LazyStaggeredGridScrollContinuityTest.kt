package my.nanihadesuka.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import my.nanihadesuka.compose.controller.LazyStaggeredGridStateController
import my.nanihadesuka.compose.controller.rememberLazyStaggeredGridStateController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.random.Random

/**
 * Simulates how a user scrolls a staggered grid (small pixel steps, like a finger drag) and
 * how a user drags the scrollbar thumb, then checks the thumb never moves backward
 * (C0 continuity + monotonicity) in both directions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class LazyStaggeredGridScrollContinuityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class Setup(
        val orientation: Orientation,
        val reverseLayout: Boolean = false,
        val contentPadding: Dp = 0.dp,
        val items: Int = 101,
    )

    private lateinit var controller: LazyStaggeredGridStateController
    private val state = LazyStaggeredGridState()

    private fun setContent(setup: Setup) {
        val random = Random(42)
        val aspectRatios = List(setup.items) { random.nextFloat() + 0.5f }
        composeRule.setContent {
            controller = rememberLazyStaggeredGridStateController(
                state = state,
                reverseLayout = setup.reverseLayout,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
                orientation = setup.orientation,
            )
            when (setup.orientation) {
                Orientation.Vertical -> LazyVerticalStaggeredGrid(
                    state = state,
                    columns = StaggeredGridCells.Adaptive(minSize = 128.dp),
                    reverseLayout = setup.reverseLayout,
                    contentPadding = PaddingValues(vertical = setup.contentPadding),
                    verticalItemSpacing = 3.dp,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(setup.items) { Box(Modifier.aspectRatio(aspectRatios[it])) }
                }

                Orientation.Horizontal -> LazyHorizontalStaggeredGrid(
                    state = state,
                    rows = StaggeredGridCells.Adaptive(minSize = 128.dp),
                    reverseLayout = setup.reverseLayout,
                    contentPadding = PaddingValues(horizontal = setup.contentPadding),
                    horizontalItemSpacing = 3.dp,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(setup.items) { Box(Modifier.aspectRatio(aspectRatios[it])) }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private var reverse = false

    /** Thumb progress along the scroll direction: 0 at list start, grows toward list end. */
    private fun progress(): Float = composeRule.runOnIdle {
        val offset = controller.thumbOffsetNormalized.value
        if (reverse) 1f - offset - controller.thumbSizeNormalized.value else offset
    }

    private data class Sample(val progress: Float, val size: Float, val contentPosition: Long)

    private fun sample() = Sample(
        progress(),
        composeRule.runOnIdle { controller.thumbSizeNormalized.value },
        composeRule.runOnIdle { contentPosition() },
    )

    private fun scrollWholeList(stepPx: Float): List<Sample> {
        val samples = mutableListOf(sample())
        while (true) {
            val consumed = composeRule.runOnIdle { runBlocking { state.scrollBy(stepPx) } }
            composeRule.waitForIdle()
            if (abs(consumed) < 0.5f) break
            samples.add(sample())
        }
        return samples
    }

    private fun assertContinuous(samples: List<Sample>, forward: Boolean, label: String) {
        assert(samples.size > 50) { "$label: list did not scroll (${samples.size} samples)" }
        for (i in 1 until samples.size) {
            val prev = samples[i - 1]
            val cur = samples[i]
            val delta = cur.progress - prev.progress
            // The staggered grid can overshoot the list end by a few pixels and snap back on the
            // next measure; the thumb must follow the content then.
            val contentWentBack = if (forward) cur.contentPosition < prev.contentPosition
            else cur.contentPosition > prev.contentPosition
            val backward = if (forward) delta < -1e-4f else delta > 1e-4f
            assert(!backward || contentWentBack) {
                "$label: thumb moved backward at step $i/${samples.size - 1}: " +
                    "${prev.progress} -> ${cur.progress}"
            }
            assert(abs(delta) <= 0.01f) {
                "$label: thumb jumped at step $i/${samples.size - 1}: " +
                    "${prev.progress} -> ${cur.progress}"
            }
            assert(abs(cur.size - prev.size) <= 0.004f) {
                "$label: thumb size jumped at step $i/${samples.size - 1}: " +
                    "${prev.size} -> ${cur.size}"
            }
        }
    }

    private fun assertEndsReached(forward: List<Sample>, label: String) {
        val first = forward.first()
        val last = forward.last()
        assert(first.progress < 0.005f) { "$label: thumb not at start: ${first.progress}" }
        assert(last.progress + last.size > 0.995f) {
            "$label: thumb not at end: offset=${last.progress} size=${last.size}"
        }
    }

    private fun assertScrollBothWays(setup: Setup, label: String) {
        reverse = setup.reverseLayout
        setContent(setup)
        val forward = scrollWholeList(7f)
        assertContinuous(forward, forward = true, "$label forward")
        assertEndsReached(forward, label)
        assertContinuous(scrollWholeList(-7f), forward = false, "$label backward")
    }

    // ==================== User scrolls the grid ====================


    @Test
    fun `vertical staggered grid - demo setup`() =
        assertScrollBothWays(Setup(Orientation.Vertical), "vertical")

    @Test
    fun `horizontal staggered grid - demo setup`() =
        assertScrollBothWays(Setup(Orientation.Horizontal), "horizontal")

    @Test
    fun `vertical staggered grid - reverse layout`() =
        assertScrollBothWays(Setup(Orientation.Vertical, reverseLayout = true), "vertical reverse")

    @Test
    fun `vertical staggered grid - content padding`() =
        assertScrollBothWays(Setup(Orientation.Vertical, contentPadding = 300.dp), "vertical padding")

    // ==================== User drags the scrollbar thumb ====================

    /** Monotonic content position key: index * 1e6 + offset. */
    private fun contentPosition(): Long =
        state.firstVisibleItemIndex * 1_000_000L + state.firstVisibleItemScrollOffset

    private fun assertThumbDragBothWays(setup: Setup, label: String) {
        reverse = setup.reverseLayout
        setContent(setup)
        val trackPx = 2000f

        fun drag(deltaPx: Float): List<Sample> {
            val samples = mutableListOf(sample())
            composeRule.runOnIdle {
                val visualTop = controller.thumbOffsetNormalized.value * trackPx
                val grabAt = visualTop + controller.thumbSizeNormalized.value * trackPx / 2
                controller.onDragStarted(grabAt, trackPx)
            }
            var prevPosition = composeRule.runOnIdle { contentPosition() }
            repeat((trackPx / abs(deltaPx)).toInt() + 20) { step ->
                composeRule.runOnIdle { controller.onDraggableState(deltaPx, trackPx) }
                composeRule.waitForIdle()
                samples.add(sample())
                val position = composeRule.runOnIdle { contentPosition() }
                val towardEnd = (deltaPx > 0) != setup.reverseLayout
                val wentBack = if (towardEnd) position < prevPosition else position > prevPosition
                assert(!wentBack) {
                    "$label: content moved backward while dragging at step $step: $prevPosition -> $position"
                }
                prevPosition = position
            }
            composeRule.runOnIdle { controller.onDragStopped() }
            return samples
        }

        val towardEnd = if (setup.reverseLayout) -5f else 5f
        val forward = drag(towardEnd)
        assertContinuous(forward, forward = true, "$label drag toward end")
        assertEndsReached(forward, "$label drag")
        assertContinuous(drag(-towardEnd), forward = false, "$label drag toward start")
    }

    @Test
    fun `thumb drag - vertical staggered grid`() =
        assertThumbDragBothWays(Setup(Orientation.Vertical), "vertical")

    @Test
    fun `thumb drag - horizontal staggered grid`() =
        assertThumbDragBothWays(Setup(Orientation.Horizontal), "horizontal")
}
