package my.nanihadesuka.compose

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import my.nanihadesuka.compose.controller.LazyListStateController
import my.nanihadesuka.compose.controller.rememberLazyListStateController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

/**
 * Simulates how a user scrolls a LazyColumn (small pixel steps, like a finger drag) and
 * how a user drags the scrollbar thumb, then checks the thumb never moves backward
 * (C0 continuity + monotonicity) in both directions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class LazyColumnScrollContinuityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class Setup(
        val reverseLayout: Boolean,
        val contentPadding: Dp,
        val headers: Int,
        val headerHeight: Dp = 36.dp,
        val items: Int = 101,
        val itemHeight: (Int) -> Dp = { 96.dp },
    )

    private lateinit var controller: LazyListStateController
    private val state = LazyListState()

    private fun setContent(setup: Setup) {
        composeRule.setContent {
            controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            LazyColumn(
                state = state,
                reverseLayout = setup.reverseLayout,
                contentPadding = PaddingValues(vertical = setup.contentPadding),
                modifier = Modifier.fillMaxSize(),
            ) {
                repeat(setup.headers) {
                    stickyHeader { Box(Modifier.fillMaxWidth().height(setup.headerHeight)) }
                }
                items(setup.items) { Box(Modifier.fillMaxWidth().height(setup.itemHeight(it))) }
            }
        }
        composeRule.waitForIdle()
    }

    /** Thumb progress along the scroll direction: 0 at list start, grows toward list end. */
    private fun progress(): Float = composeRule.runOnIdle {
        val offset = controller.thumbOffsetNormalized.value
        if (state.layoutInfo.reverseLayout) 1f - offset - controller.thumbSizeNormalized.value
        else offset
    }

    private fun thumbSize(): Float = composeRule.runOnIdle { controller.thumbSizeNormalized.value }

    private data class Sample(val progress: Float, val size: Float)

    private fun sample() = Sample(progress(), thumbSize())

    /** Scrolls the list like a finger until the end is reached, returning a sample per step. */
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
            val backward = if (forward) delta < -1e-4f else delta > 1e-4f
            assert(!backward) {
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

    private fun assertScrollBothWays(setup: Setup, label: String) {
        setContent(setup)
        assertContinuous(scrollWholeList(7f), forward = true, "$label forward")
        assertContinuous(scrollWholeList(-7f), forward = false, "$label backward")
    }

    // ==================== User scrolls the list ====================

    @Test
    fun `demo setup - reverse layout, content padding, 4 sticky headers`() =
        assertScrollBothWays(Setup(reverseLayout = true, contentPadding = 300.dp, headers = 4), "demo")

    @Test
    fun `content padding only`() =
        assertScrollBothWays(Setup(reverseLayout = false, contentPadding = 300.dp, headers = 0), "padding")

    @Test
    fun `reverse layout with content padding`() =
        assertScrollBothWays(Setup(reverseLayout = true, contentPadding = 300.dp, headers = 0), "reverse+padding")

    @Test
    fun `sticky headers only`() =
        assertScrollBothWays(Setup(reverseLayout = false, contentPadding = 0.dp, headers = 4), "headers")

    @Test
    fun `many sticky headers with small padding`() =
        assertScrollBothWays(Setup(reverseLayout = false, contentPadding = 10.dp, headers = 31), "many headers")

    // ==================== User drags the scrollbar thumb ====================

    private fun assertThumbDragBothWays(setup: Setup, label: String) {
        setContent(setup)
        val trackPx = 2000f

        fun drag(deltaPx: Float): List<Pair<Sample, Long>> {
            val samples = mutableListOf<Pair<Sample, Long>>()
            composeRule.runOnIdle {
                val visualTop = controller.thumbOffsetNormalized.value * trackPx
                val grabAt = visualTop + controller.thumbSizeNormalized.value * trackPx / 2
                controller.onDragStarted(grabAt, trackPx)
            }
            repeat((trackPx / abs(deltaPx)).toInt() + 20) {
                composeRule.runOnIdle { controller.onDraggableState(deltaPx, trackPx) }
                composeRule.waitForIdle()
                samples.add(sample() to composeRule.runOnIdle { contentPosition() })
            }
            composeRule.runOnIdle { controller.onDragStopped() }
            return samples
        }

        // Screen-space drag direction that moves toward the list end.
        val towardEnd = if (setup.reverseLayout) -5f else 5f
        val forward = drag(towardEnd)
        val backward = drag(-towardEnd)

        for ((samples, isForward, name) in listOf(
            Triple(forward, true, "$label drag toward end"),
            Triple(backward, false, "$label drag toward start"),
        )) {
            assertContinuous(listOf(samples.first().first) + samples.map { it.first }, isForward, name)
            for (i in 1 until samples.size) {
                val prev = samples[i - 1].second
                val cur = samples[i].second
                val wentBack = if (isForward) cur < prev else cur > prev
                assert(!wentBack) { "$name: content moved backward at step $i: $prev -> $cur" }
            }
        }
    }

    /** Monotonic content position key: index * 1e6 + offset. */
    private fun contentPosition(): Long =
        state.firstVisibleItemIndex * 1_000_000L + state.firstVisibleItemScrollOffset

    @Test
    fun `thumb drag - demo setup`() =
        assertThumbDragBothWays(Setup(reverseLayout = true, contentPadding = 300.dp, headers = 4), "demo")

    @Test
    fun `thumb drag - plain list`() =
        assertThumbDragBothWays(Setup(reverseLayout = false, contentPadding = 0.dp, headers = 0), "plain")
}
