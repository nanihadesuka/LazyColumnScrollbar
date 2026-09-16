package my.nanihadesuka.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import my.nanihadesuka.compose.controller.rememberLazyGridStateController
import my.nanihadesuka.compose.controller.rememberLazyListStateController
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for scrollbar edge cases: grid incomplete rows, sticky header jumps,
 * and C0 continuity of the thumb offset function.
 *
 * Thumb size fluctuation with variable-height items is expected behavior
 * (lazy lists can't measure non-visible items). These tests focus on
 * actual position discontinuities (jerkiness).
 */
@RunWith(RobolectricTestRunner::class)
class ScrollbarEdgeCaseTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ==================== Grid: incomplete last row jump ====================
    // Uses controller state directly to avoid ScrollbarRobot normalization artifacts

    @Test
    fun `LazyVerticalGrid should not jump at incomplete last row - 3 columns 10 items`() {
        assertGridRowByRowSmooth(itemCount = 10, columns = 3, itemHeight = 200.dp)
    }

    @Test
    fun `LazyVerticalGrid should not jump at incomplete last row - 3 columns 16 items`() {
        assertGridRowByRowSmooth(itemCount = 16, columns = 3, itemHeight = 150.dp)
    }

    @Test
    fun `LazyVerticalGrid should not jump at incomplete last row - 5 columns 23 items`() {
        assertGridRowByRowSmooth(itemCount = 53, columns = 5, itemHeight = 100.dp)
    }

    private fun assertGridRowByRowSmooth(itemCount: Int, columns: Int, itemHeight: Dp) {
        val state = LazyGridState()
        val offsets = mutableListOf<Float>()
        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyGridStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
                orientation = Orientation.Vertical,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyVerticalGrid(
                state = state,
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(itemCount, key = { it }) {
                    Box(Modifier.fillMaxWidth().height(itemHeight))
                }
            }
        }

        val rowCount = (itemCount + columns - 1) / columns
        for (row in 0 until rowCount) {
            val flatIndex = row * columns
            composeRule.runOnIdle { runBlocking { state.scrollToItem(flatIndex) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        if (offsets.totalRange() <= 0f) return
        assertMonotonic(offsets, "grid row-by-row scroll")
        assertNoLargeJumps(offsets, maxJumpFactor = 3f)
    }

    // ==================== Sticky header position jumps ====================

    @Test
    fun `LazyColumn headers should not cause position jumps`() {
        val state = LazyListState()
        setLazyColumnWithDifferentHeightHeaders(
            state = state,
            totalItems = 100,
            headerEveryN = 10,
            headerHeight = 80.dp,
            itemHeight = 30.dp,
        )

        val totalNodes = 100 + 10
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "scroll with headers")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    @Test
    fun `LazyColumn variable items with headers should not have large jumps`() {
        val state = LazyListState()
        setLazyColumnVariableWithDifferentHeightHeaders(
            state = state,
            totalItems = 100,
            headerEveryN = 10,
            headerHeight = 60.dp,
            heightForIndex = { if (it % 2 == 0) 20.dp else 150.dp },
        )

        val totalNodes = 100 + 10
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "variable items + headers")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    // ==================== C0 continuity via state controllers ====================
    // C0 continuity: the thumb offset function has no discontinuities.
    // Tested by reading thumbOffsetNormalized directly from the controller,
    // scrolling in fine-grained pixel increments, and checking that consecutive
    // values don't have gaps larger than a tolerance.

    @Test
    fun `C0 continuity - LazyColumn uniform items - item-by-item`() {
        val itemCount = 100
        val state = LazyListState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                items(itemCount, key = { it }) {
                    Box(Modifier.fillMaxWidth().height(60.dp))
                }
            }
        }

        for (index in 0 until itemCount) {
            composeRule.runOnIdle { runBlocking { state.scrollToItem(index) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 2f / itemCount, label = "uniform items")
    }

    @Test
    fun `C0 continuity - LazyColumn uniform items - sub-item pixel scroll`() {
        val itemCount = 100
        val state = LazyListState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                items(itemCount, key = { it }) {
                    Box(Modifier.fillMaxWidth().height(60.dp))
                }
            }
        }

        val pixelStep = 15f
        repeat(200) {
            composeRule.runOnIdle { runBlocking { state.scrollBy(pixelStep) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 3f / itemCount, label = "uniform pixel scroll")
    }

    @Test
    fun `C0 continuity - LazyColumn variable items - item-by-item`() {
        val itemCount = 100
        val state = LazyListState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                items(itemCount, key = { it }) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(if (index % 2 == 0) 20.dp else 200.dp)
                    )
                }
            }
        }

        for (index in 0 until itemCount) {
            composeRule.runOnIdle { runBlocking { state.scrollToItem(index) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 2f / itemCount, label = "variable items")
    }

    @Test
    fun `C0 continuity - LazyColumn variable items - sub-item pixel scroll`() {
        val itemCount = 100
        val state = LazyListState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                items(itemCount, key = { it }) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(if (index % 2 == 0) 20.dp else 200.dp)
                    )
                }
            }
        }

        val pixelStep = 10f
        repeat(300) {
            composeRule.runOnIdle { runBlocking { state.scrollBy(pixelStep) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 3f / itemCount, label = "variable pixel scroll")
    }

    @Test
    fun `C0 continuity - LazyColumn with sticky headers - item-by-item`() {
        val state = LazyListState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)
        val totalNodes = 100 + 10

        composeRule.setContent {
            val controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                var itemIndex = 0
                var sectionIndex = 0
                while (itemIndex < 100) {
                    if (itemIndex % 10 == 0) {
                        stickyHeader(key = "header_$sectionIndex") {
                            Box(Modifier.fillMaxWidth().height(80.dp))
                        }
                        sectionIndex++
                    }
                    val batchEnd = minOf(itemIndex + 10 - (itemIndex % 10), 100)
                    val batchStart = itemIndex
                    items(batchEnd - batchStart) {
                        Box(Modifier.fillMaxWidth().height(30.dp))
                    }
                    itemIndex = batchEnd
                }
            }
        }

        for (index in 0 until totalNodes) {
            composeRule.runOnIdle { runBlocking { state.scrollToItem(index) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 2f / totalNodes, label = "sticky headers")
    }

    @Test
    fun `C0 continuity - LazyColumn with sticky headers - sub-item pixel scroll`() {
        val state = LazyListState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyListStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                var itemIndex = 0
                var sectionIndex = 0
                while (itemIndex < 100) {
                    if (itemIndex % 10 == 0) {
                        stickyHeader(key = "header_$sectionIndex") {
                            Box(Modifier.fillMaxWidth().height(80.dp))
                        }
                        sectionIndex++
                    }
                    val batchEnd = minOf(itemIndex + 10 - (itemIndex % 10), 100)
                    val batchStart = itemIndex
                    items(batchEnd - batchStart) {
                        Box(Modifier.fillMaxWidth().height(30.dp))
                    }
                    itemIndex = batchEnd
                }
            }
        }

        val pixelStep = 10f
        repeat(300) {
            composeRule.runOnIdle { runBlocking { state.scrollBy(pixelStep) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 0.03f, label = "sticky headers pixel scroll")
    }

    @Test
    fun `C0 continuity - LazyVerticalGrid - item-by-item`() {
        val itemCount = 30
        val state = LazyGridState()
        val offsets = mutableListOf<Float>()

        val thumbOffset = mutableFloatStateOf(0f)

        composeRule.setContent {
            val controller = rememberLazyGridStateController(
                state = state,
                thumbMinLength = 0.1f,
                thumbMaxLength = 1.0f,
                alwaysShowScrollBar = true,
                selectionMode = ScrollbarSelectionMode.Thumb,
                orientation = Orientation.Vertical,
            )
            thumbOffset.floatValue = controller.thumbOffsetNormalized.value

            LazyVerticalGrid(
                state = state,
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(itemCount, key = { it }) {
                    Box(Modifier.fillMaxWidth().height(100.dp).padding(4.dp))
                }
            }
        }

        for (index in 0 until itemCount) {
            composeRule.runOnIdle { runBlocking { state.scrollToItem(index) } }
            composeRule.waitForIdle()
            composeRule.runOnIdle { offsets.add(thumbOffset.floatValue) }
        }

        assertC0Continuity(offsets, maxGap = 3f / (itemCount / 3), label = "grid items")
    }

    // ==================== Assertions ====================

    private fun assertMonotonic(positions: List<Float>, direction: String) {
        for (i in 1 until positions.size) {
            assert(positions[i] >= positions[i - 1]) {
                "Thumb moved backward when scrolling $direction at step $i/${positions.size - 1}: " +
                    "${positions[i - 1]} -> ${positions[i]}"
            }
        }
    }

    private fun assertNoLargeJumps(positions: List<Float>, maxJumpFactor: Float) {
        if (positions.size < 3) return
        val totalRange = positions.totalRange()
        if (totalRange <= 0f) return

        val avgStep = totalRange / (positions.size - 1)
        val epsilon = 0.001f
        for (i in 1 until positions.size) {
            val step = positions[i] - positions[i - 1]
            assert(step <= maxJumpFactor * avgStep + epsilon) {
                "Large jump at step $i/${positions.size - 1}: " +
                    "jump=$step avg=$avgStep ratio=${step / avgStep} maxAllowed=${maxJumpFactor * avgStep}"
            }
        }
    }

    /**
     * C0 continuity: the offset sequence must be non-decreasing (monotonic)
     * and consecutive values must not have gaps larger than [maxGap].
     */
    private fun assertC0Continuity(offsets: List<Float>, maxGap: Float, label: String) {
        if (offsets.size < 2) return
        for (i in 1 until offsets.size) {
            val delta = offsets[i] - offsets[i - 1]
            assert(delta >= -0.001f) {
                "C0 violation ($label): backward jump at step $i/${offsets.size - 1}, " +
                    "${offsets[i - 1]} -> ${offsets[i]} (delta=$delta)"
            }
            assert(delta <= maxGap) {
                "C0 violation ($label): discontinuity at step $i/${offsets.size - 1}, " +
                    "${offsets[i - 1]} -> ${offsets[i]} (delta=$delta, maxGap=$maxGap)"
            }
        }
    }

    private fun List<Float>.totalRange() = last() - first()

    // ==================== Position collection (UI-based, for grid tests) ====================

    private fun collectLazyListVerticalPositions(
        state: LazyListState,
        itemCount: Int,
    ): List<Float> {
        val robot = ScrollbarRobot(composeRule)
        val positions = mutableListOf<Float>()
        for (index in 0 until itemCount) {
            composeRule.runOnIdle { runBlocking { state.scrollToItem(index) } }
            composeRule.waitForIdle()
            positions.add(robot.getThumbVerticalOffset())
        }
        return positions
    }

    // ==================== Content setup ====================

    private fun setLazyColumnWithDifferentHeightHeaders(
        state: LazyListState,
        totalItems: Int,
        headerEveryN: Int,
        headerHeight: Dp,
        itemHeight: Dp,
    ) {
        composeRule.setContent {
            LazyColumnScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    var itemIndex = 0
                    var sectionIndex = 0
                    while (itemIndex < totalItems) {
                        if (itemIndex % headerEveryN == 0) {
                            stickyHeader(key = "header_$sectionIndex") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(headerHeight)
                                )
                            }
                            sectionIndex++
                        }
                        val batchEnd = minOf(
                            itemIndex + headerEveryN - (itemIndex % headerEveryN),
                            totalItems
                        )
                        val batchStart = itemIndex
                        items(batchEnd - batchStart) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight)
                            )
                        }
                        itemIndex = batchEnd
                    }
                }
            }
        }
    }

    private fun setLazyColumnVariableWithDifferentHeightHeaders(
        state: LazyListState,
        totalItems: Int,
        headerEveryN: Int,
        headerHeight: Dp,
        heightForIndex: (Int) -> Dp,
    ) {
        composeRule.setContent {
            LazyColumnScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    var itemIndex = 0
                    var sectionIndex = 0
                    while (itemIndex < totalItems) {
                        if (itemIndex % headerEveryN == 0) {
                            stickyHeader(key = "header_$sectionIndex") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(headerHeight)
                                )
                            }
                            sectionIndex++
                        }
                        val batchEnd = minOf(
                            itemIndex + headerEveryN - (itemIndex % headerEveryN),
                            totalItems
                        )
                        val batchStart = itemIndex
                        items(batchEnd - batchStart) { offset ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(heightForIndex(batchStart + offset))
                            )
                        }
                        itemIndex = batchEnd
                    }
                }
            }
        }
    }
}
