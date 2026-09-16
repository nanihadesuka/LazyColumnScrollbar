package my.nanihadesuka.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.math.absoluteValue

@RunWith(RobolectricTestRunner::class)
class ScrollbarSmoothnessTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ==================== LazyColumn ====================

    @Test
    fun `LazyColumn - smooth scroll with 50 uniform items`() {
        val itemCount = 50
        val state = LazyListState()
        setLazyColumnContent(state = state, itemCount = itemCount)

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
        assertProportional(positions)
    }

    @Test
    fun `LazyColumn - smooth scroll with 100 uniform items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyColumnContent(state = state, itemCount = itemCount)

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
        assertProportional(positions)
    }

    @Test
    fun `LazyColumn - smooth scroll with 500 uniform items`() {
        val itemCount = 500
        val state = LazyListState()
        setLazyColumnContent(state = state, itemCount = itemCount)

        val positions = collectLazyListVerticalPositions(state, itemCount, step = 5)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
        assertProportional(positions)
    }

    @Test
    fun `LazyColumn - monotonic scroll with alternating height items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyColumnVariableContent(
            state = state,
            itemCount = itemCount,
            heightForIndex = { if (it % 2 == 0) 20.dp else 200.dp }
        )

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
    }

    @Test
    fun `LazyColumn - monotonic scroll with block variable height items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyColumnVariableContent(
            state = state,
            itemCount = itemCount,
            heightForIndex = { if (it < 50) 20.dp else 200.dp }
        )

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
    }

    @Test
    fun `LazyColumn - monotonic scroll with random-like height items`() {
        val itemCount = 100
        val state = LazyListState()
        val heights = listOf(20.dp, 60.dp, 150.dp, 40.dp, 200.dp, 30.dp, 100.dp)
        setLazyColumnVariableContent(
            state = state,
            itemCount = itemCount,
            heightForIndex = { heights[it % heights.size] }
        )

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
    }

    // ==================== Column (ScrollState) ====================

    @Test
    fun `Column - smooth scroll with 100 uniform items`() {
        val state = ScrollState(initial = 0)
        setColumnContent(state = state, itemCount = 100)

        val positions = collectScrollStateVerticalPositions(state, steps = 50)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
        assertProportional(positions)
    }

    @Test
    fun `Column - monotonic scroll with variable height items`() {
        val state = ScrollState(initial = 0)
        setColumnVariableContent(
            state = state,
            itemCount = 100,
            heightForIndex = { if (it % 2 == 0) 20.dp else 200.dp }
        )

        val positions = collectScrollStateVerticalPositions(state, steps = 50)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    // ==================== LazyVerticalGrid ====================

    @Test
    fun `LazyVerticalGrid - smooth scroll with 100 uniform items`() {
        val itemCount = 100
        val state = LazyGridState()
        setLazyVerticalGridContent(state = state, itemCount = itemCount)

        val positions = collectLazyGridVerticalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    // ==================== LazyRow ====================

    @Test
    fun `LazyRow - smooth scroll with 100 uniform items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyRowContent(state = state, itemCount = itemCount)

        val positions = collectLazyListHorizontalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "right")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    @Test
    fun `LazyRow - monotonic scroll with variable width items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyRowVariableContent(
            state = state,
            itemCount = itemCount,
            widthForIndex = { if (it % 2 == 0) 20.dp else 200.dp }
        )

        val positions = collectLazyListHorizontalPositions(state, itemCount)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "right")
    }

    // ==================== LazyColumn - Reverse Layout ====================

    @Test
    fun `LazyColumn reverse - smooth scroll with 100 uniform items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyColumnContent(state = state, itemCount = itemCount, reverseLayout = true)

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange().absoluteValue <= 0f) return

        assertMonotonicDecreasing(positions, "down (reverse)")
        assertNoLargeJumps(positions, maxJumpFactor = 3f, absolute = true)
    }

    @Test
    fun `LazyColumn reverse - monotonic scroll with variable height items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyColumnVariableContent(
            state = state,
            itemCount = itemCount,
            heightForIndex = { if (it % 2 == 0) 20.dp else 200.dp },
            reverseLayout = true,
        )

        val positions = collectLazyListVerticalPositions(state, itemCount)
        if (positions.totalRange().absoluteValue <= 0f) return

        assertMonotonicDecreasing(positions, "down (reverse)")
    }

    @Test
    fun `LazyRow reverse - smooth scroll with 100 uniform items`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyRowContent(state = state, itemCount = itemCount, reverseLayout = true)

        val positions = collectLazyListHorizontalPositions(state, itemCount)
        if (positions.totalRange().absoluteValue <= 0f) return

        assertMonotonicDecreasing(positions, "right (reverse)")
        assertNoLargeJumps(positions, maxJumpFactor = 3f, absolute = true)
    }

    // ==================== LazyColumn - Sticky Headers ====================

    @Test
    fun `LazyColumn - smooth scroll with single sticky header`() {
        val itemCount = 100
        val state = LazyListState()
        setLazyColumnWithHeadersContent(
            state = state,
            totalItems = itemCount,
            headerEveryN = 0,
            headerAtStart = true,
        )

        val totalNodes = itemCount + 1
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    @Test
    fun `LazyColumn - smooth scroll with headers every 10 items`() {
        val itemsPerSection = 10
        val sectionCount = 10
        val state = LazyListState()
        setLazyColumnWithHeadersContent(
            state = state,
            totalItems = itemsPerSection * sectionCount,
            headerEveryN = itemsPerSection,
        )

        val totalNodes = itemsPerSection * sectionCount + sectionCount
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    @Test
    fun `LazyColumn - smooth scroll with headers every 5 items`() {
        val itemsPerSection = 5
        val sectionCount = 20
        val state = LazyListState()
        setLazyColumnWithHeadersContent(
            state = state,
            totalItems = itemsPerSection * sectionCount,
            headerEveryN = itemsPerSection,
        )

        val totalNodes = itemsPerSection * sectionCount + sectionCount
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    @Test
    fun `LazyColumn - smooth scroll with consecutive headers`() {
        val state = LazyListState()
        setLazyColumnWithConsecutiveHeadersContent(
            state = state,
            itemCount = 80,
            consecutiveHeaderCount = 5,
            headerInsertAt = 20,
        )

        val totalNodes = 80 + 5
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
        assertNoLargeJumps(positions, maxJumpFactor = 3f)
    }

    @Test
    fun `LazyColumn - smooth scroll with variable items and headers`() {
        val state = LazyListState()
        setLazyColumnVariableWithHeadersContent(
            state = state,
            totalItems = 100,
            headerEveryN = 10,
            heightForIndex = { if (it % 3 == 0) 20.dp else 120.dp },
        )

        val totalNodes = 100 + 10
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange() <= 0f) return

        assertMonotonic(positions, "down")
    }

    @Test
    fun `LazyColumn reverse - smooth scroll with headers every 10 items`() {
        val itemsPerSection = 10
        val sectionCount = 10
        val state = LazyListState()
        setLazyColumnWithHeadersContent(
            state = state,
            totalItems = itemsPerSection * sectionCount,
            headerEveryN = itemsPerSection,
            reverseLayout = true,
        )

        val totalNodes = itemsPerSection * sectionCount + sectionCount
        val positions = collectLazyListVerticalPositions(state, totalNodes)
        if (positions.totalRange().absoluteValue <= 0f) return

        assertMonotonicDecreasing(positions, "down (reverse)")
    }

    // ==================== Position collection helpers ====================

    private fun collectLazyListVerticalPositions(
        state: LazyListState,
        itemCount: Int,
        step: Int = 1,
    ): List<Float> {
        val robot = ScrollbarRobot(composeRule)
        val positions = mutableListOf<Float>()
        for (index in 0 until itemCount step step) {
            composeRule.runOnIdle {
                runBlocking { state.scrollToItem(index) }
            }
            composeRule.waitForIdle()
            positions.add(robot.getThumbVerticalOffset())
        }
        return positions
    }

    private fun collectLazyListHorizontalPositions(
        state: LazyListState,
        itemCount: Int,
        step: Int = 1,
    ): List<Float> {
        val robot = ScrollbarRobot(composeRule)
        val positions = mutableListOf<Float>()
        for (index in 0 until itemCount step step) {
            composeRule.runOnIdle {
                runBlocking { state.scrollToItem(index) }
            }
            composeRule.waitForIdle()
            positions.add(robot.getThumbHorizontalOffset())
        }
        return positions
    }

    private fun collectLazyGridVerticalPositions(
        state: LazyGridState,
        itemCount: Int,
        step: Int = 1,
    ): List<Float> {
        val robot = ScrollbarRobot(composeRule)
        val positions = mutableListOf<Float>()
        for (index in 0 until itemCount step step) {
            composeRule.runOnIdle {
                runBlocking { state.scrollToItem(index) }
            }
            composeRule.waitForIdle()
            positions.add(robot.getThumbVerticalOffset())
        }
        return positions
    }

    private fun collectScrollStateVerticalPositions(
        state: ScrollState,
        steps: Int,
    ): List<Float> {
        val robot = ScrollbarRobot(composeRule)
        val positions = mutableListOf<Float>()
        var maxValue = 0
        composeRule.runOnIdle { maxValue = state.maxValue }
        if (maxValue <= 0) return listOf(0f)

        for (step in 0..steps) {
            val pixelValue = (maxValue.toLong() * step / steps).toInt()
            composeRule.runOnIdle {
                runBlocking { state.scrollTo(pixelValue) }
            }
            composeRule.waitForIdle()
            positions.add(robot.getThumbVerticalOffset())
        }
        return positions
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

    private fun assertMonotonicDecreasing(positions: List<Float>, direction: String) {
        for (i in 1 until positions.size) {
            assert(positions[i] <= positions[i - 1]) {
                "Thumb moved forward when scrolling $direction at step $i/${positions.size - 1}: " +
                    "${positions[i - 1]} -> ${positions[i]}"
            }
        }
    }

    private fun assertNoLargeJumps(
        positions: List<Float>,
        maxJumpFactor: Float,
        absolute: Boolean = false,
    ) {
        if (positions.size < 3) return
        val totalRange = if (absolute) positions.totalRange().absoluteValue else positions.totalRange()
        if (totalRange <= 0f) return

        val avgStep = totalRange / (positions.size - 1)
        for (i in 1 until positions.size) {
            val step = if (absolute) {
                (positions[i] - positions[i - 1]).absoluteValue
            } else {
                positions[i] - positions[i - 1]
            }
            assert(step <= maxJumpFactor * avgStep) {
                "Large jump at step $i/${positions.size - 1}: " +
                    "jump=$step avg=$avgStep maxAllowed=${maxJumpFactor * avgStep}"
            }
        }
    }

    private fun assertProportional(positions: List<Float>, tolerance: Float = 0.15f) {
        if (positions.size < 3) return
        val totalRange = positions.totalRange()
        if (totalRange <= 0f) return

        val midIndex = positions.size / 2
        val expectedMid = totalRange / 2f + positions.first()
        val actualMid = positions[midIndex]
        assert((actualMid - expectedMid).absoluteValue < tolerance) {
            "Non-proportional: at step $midIndex/${positions.size - 1}, " +
                "expected ~$expectedMid but got $actualMid"
        }
    }

    private fun List<Float>.totalRange() = last() - first()

    // ==================== Content setup ====================

    private fun setLazyColumnContent(
        state: LazyListState,
        itemCount: Int,
        reverseLayout: Boolean = false,
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
                    reverseLayout = reverseLayout,
                ) {
                    items(itemCount, key = { it }) {
                        Text(
                            text = "Item $it",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }

    private fun setLazyColumnVariableContent(
        state: LazyListState,
        itemCount: Int,
        heightForIndex: (Int) -> Dp,
        reverseLayout: Boolean = false,
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
                    reverseLayout = reverseLayout,
                ) {
                    items(itemCount, key = { it }) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heightForIndex(index))
                        )
                    }
                }
            }
        }
    }

    private fun setLazyColumnWithHeadersContent(
        state: LazyListState,
        totalItems: Int,
        headerEveryN: Int,
        headerAtStart: Boolean = headerEveryN > 0,
        reverseLayout: Boolean = false,
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
                    reverseLayout = reverseLayout,
                ) {
                    var itemIndex = 0
                    var sectionIndex = 0
                    while (itemIndex < totalItems) {
                        if (headerAtStart || (headerEveryN > 0 && itemIndex > 0 && itemIndex % headerEveryN == 0)) {
                            if (headerAtStart && itemIndex == 0 || (headerEveryN > 0 && itemIndex % headerEveryN == 0)) {
                                stickyHeaderItem(sectionIndex)
                                sectionIndex++
                            }
                        }
                        val batchEnd = if (headerEveryN > 0) {
                            minOf(itemIndex + headerEveryN - (itemIndex % headerEveryN), totalItems)
                        } else {
                            totalItems
                        }
                        val batchStart = itemIndex
                        items(batchEnd - batchStart) { offset ->
                            Text(
                                text = "Item ${batchStart + offset}",
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                        itemIndex = batchEnd
                    }
                }
            }
        }
    }

    private fun setLazyColumnWithConsecutiveHeadersContent(
        state: LazyListState,
        itemCount: Int,
        consecutiveHeaderCount: Int,
        headerInsertAt: Int,
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
                    items(headerInsertAt) { index ->
                        Text(
                            text = "Item $index",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                    repeat(consecutiveHeaderCount) { headerIdx ->
                        stickyHeaderItem(headerIdx)
                    }
                    items(itemCount - headerInsertAt) { offset ->
                        Text(
                            text = "Item ${headerInsertAt + offset}",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }

    private fun setLazyColumnVariableWithHeadersContent(
        state: LazyListState,
        totalItems: Int,
        headerEveryN: Int,
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
                            stickyHeaderItem(sectionIndex)
                            sectionIndex++
                        }
                        val batchEnd = minOf(itemIndex + headerEveryN - (itemIndex % headerEveryN), totalItems)
                        val batchStart = itemIndex
                        items(batchEnd - batchStart) { offset ->
                            val idx = batchStart + offset
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(heightForIndex(idx))
                            )
                        }
                        itemIndex = batchEnd
                    }
                }
            }
        }
    }

    private fun LazyListScope.stickyHeaderItem(sectionIndex: Int) {
        stickyHeader(key = "header_$sectionIndex") {
            Text(
                text = "Header $sectionIndex",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(8.dp)
            )
        }
    }

    private fun setColumnContent(
        state: ScrollState,
        itemCount: Int,
    ) {
        composeRule.setContent {
            ColumnScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                Column(Modifier.verticalScroll(state)) {
                    repeat(itemCount) {
                        Text(
                            text = "Item $it",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }

    private fun setColumnVariableContent(
        state: ScrollState,
        itemCount: Int,
        heightForIndex: (Int) -> Dp,
    ) {
        composeRule.setContent {
            ColumnScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                Column(Modifier.verticalScroll(state)) {
                    repeat(itemCount) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heightForIndex(index))
                        )
                    }
                }
            }
        }
    }

    private fun setLazyVerticalGridContent(
        state: LazyGridState,
        itemCount: Int,
    ) {
        composeRule.setContent {
            LazyVerticalGridScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                LazyVerticalGrid(
                    state = state,
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    items(itemCount, key = { it }) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }

    private fun setLazyRowContent(
        state: LazyListState,
        itemCount: Int,
        reverseLayout: Boolean = false,
    ) {
        composeRule.setContent {
            LazyRowScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                LazyRow(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = reverseLayout,
                ) {
                    items(itemCount, key = { it }) {
                        Text(
                            text = "Item $it",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }

    private fun setLazyRowVariableContent(
        state: LazyListState,
        itemCount: Int,
        widthForIndex: (Int) -> Dp,
    ) {
        composeRule.setContent {
            LazyRowScrollbar(
                state = state,
                settings = ScrollbarSettings(
                    alwaysShowScrollbar = true,
                    thumbUnselectedColor = Color(0xFF2A59B6),
                    thumbSelectedColor = Color(0xFF5281CA),
                ),
            ) {
                LazyRow(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(itemCount, key = { it }) { index ->
                        Box(
                            modifier = Modifier
                                .width(widthForIndex(index))
                                .height(48.dp)
                        )
                    }
                }
            }
        }
    }
}
