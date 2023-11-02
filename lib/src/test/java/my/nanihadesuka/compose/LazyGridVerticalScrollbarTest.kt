package my.nanihadesuka.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class LazyGridVerticalScrollbarTest(private val itemCount: Int) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = " Items count: {0} ")
        fun parametrizeListItemCount() =
            listOf<Array<Int>>(
                arrayOf(0),
                arrayOf(1),
                arrayOf(10),
                arrayOf(100),
                arrayOf(1000)
            )
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `scrollbar enabled`() {
        setContent(enabled = true)
        scrollbarScreen(composeRule) {
            assert { isEnabled() }
        }
    }

    @Test
    fun `scrollbar disabled`() {
        setContent(enabled = false)
        scrollbarScreen(composeRule) {
            assert { isDisabled() }
        }
    }

    @Test
    fun `scrollbar is at right side`() {
        setContent(rightSide = true)
        scrollbarScreen(composeRule) {
            assert { isAtRightSide() }
        }
    }

    @Test
    fun `scrollbar is at left side`() {
        setContent(rightSide = false)
        scrollbarScreen(composeRule) {
            assert { isAtLeftSide() }
        }
    }

    @Test
    fun `scrollbar is at right side - with indicator`() {
        setContent(rightSide = true, indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            assert {
                isAtRightSide(indicatorVisible = true)
            }
        }
    }

    @Test
    fun `scrollbar is at left side - with indicator`() {
        setContent(rightSide = false, indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            assert {
                isAtLeftSide(indicatorVisible = true)
            }
        }
    }

    @Test
    fun `move scrollbar to the top - with reverse layout`() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            moveScrollbarToTop()
            assert {
                isAtTop()
                isItemVisible(itemTag = itemTestTag(itemCount - 1))
            }
        }
    }

    @Test
    fun `move scrollbar to the bottom - with reverse layout`() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            moveScrollbarToBottom()
            assert {
                isAtBottom()
                isItemVisible(itemTag = itemTestTag(0))
            }
        }
    }

    @Test
    fun `move scrollbar to the top`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            moveScrollbarToBottom()
            assert { isAtBottom() }
            moveScrollbarToTop()
            assert {
                isAtTop()
                isItemVisible(itemTag = itemTestTag(0))
            }
        }
    }

    @Test
    fun `move scrollbar to the bottom`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            moveScrollbarToTop()
            assert { isAtTop() }
            moveScrollbarToBottom()
            assert {
                isAtBottom()
                isItemVisible(itemTag = itemTestTag(itemCount - 1))
            }
        }
    }

    @Test
    fun `move scrollbar to the bottom - with indicator`() {
        if (itemCount == 0) return

        setContent(indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            moveScrollbarToBottom(startFrom = 0.05f)
            assert { isAtBottom(indicatorVisible = true) }
        }
    }

    @Test
    fun `move scrollbar to the top - with indicator`() {
        if (itemCount == 0) return

        setContent(indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            moveScrollbarToBottom()
            assert { isAtBottom(indicatorVisible = true) }
            moveScrollbarToTop()
            assert { isAtTop(indicatorVisible = true) }
        }
    }

    @Test
    fun `always show scrollbar false`() {
        if (itemCount == 0) return

        setContent(
            alwaysShowScrollBar = false,
        )

        scrollbarScreen(composeRule) {
            // not visible without scrolling
            assert { isItemHidden(TestTagsScrollbar.scrollbar) }
        }
    }

    @Test
    fun `always show scrollbar true`() {
        if (itemCount == 0) return

        setContent(
            alwaysShowScrollBar = true,
        )

        scrollbarScreen(composeRule) {
            // always visible without scrolling
            assert { isItemVisible(TestTagsScrollbar.scrollbar) }
        }
    }

    @Test
    fun `scroll list to the bottom`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isAtBottom() }
        }
    }

    @Test
    fun `scroll list to the top`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isAtBottom() }
            scrollListToItem(testTag = itemTestTag(0))
            assert { isAtTop() }
        }
    }

    @Test
    fun `scroll list to the bottom - with reverse layout`() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isAtTop() }
            scrollListToItem(testTag = itemTestTag(0))
            assert { isAtBottom() }
        }
    }

    @Test
    fun `scroll list to the top - with reverse layout `() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isAtTop() }
        }
    }

    @Test
    fun `shows scrollbar and list`() {
        // Check it can handle empty list, small list and large lists
        setContent()
        scrollbarScreen(composeRule) {
            assert { isEnabled() }
        }
    }

    @Test
    fun `correct scrollbar padding`() {
        setContent(padding = 10.dp)
        scrollbarScreen(composeRule) {
            assert {
                hasScrollbarPadding(10.dp)
            }
        }
    }

    @Test
    fun `correct scrollbar thickness`() {
        setContent(thickness = 7.dp)
        scrollbarScreen(composeRule) {
            assert {
                hasScrollbarThickness(7.dp)
            }
        }
    }

    @Test
    fun `scrollbar thumb min height`() {
        setContent(thumbMinHeight = 0.2f)
        scrollbarScreen(composeRule) {
            assert {
                hasThumbMinHeightOrGreater(minValue = 0.2f)
            }
        }
    }

    @Test
    fun `scrollbar selection mode - Disabled`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Disabled,
            thumbMinHeight = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.05f)
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.5f)
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.95f)
            assert { isAtTop() }
        }
    }

    @Test
    fun `scrollbar selection mode - Thumb`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Thumb,
            thumbMinHeight = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert {
                isAtTop()
                hasThumbMinHeightOrGreater(0.1f)
            }
            moveScrollbarToBottom(startFrom = 0.05f)
            assert { isAtBottom() }
            moveScrollbarToTop(startFrom = 0.95f)
            assert { isAtTop() }

            // Now try select outside thumb area
            if (getThumbHeight() < 0.11f) {
                moveScrollbarToBottom(startFrom = 0.2f)
                assert { isAtTop() }
                moveScrollbarToBottom(startFrom = 0.6f)
                assert { isAtTop() }
            } else if (getThumbHeight() < 0.4f) {
                moveScrollbarToBottom(startFrom = 0.5f)
                assert { isAtTop() }
                moveScrollbarToBottom(startFrom = 0.8f)
                assert { isAtTop() }
            } else if (getThumbHeight() < 0.6f) {
                moveScrollbarToBottom(startFrom = 0.7f)
                assert { isAtTop() }
                moveScrollbarToBottom(startFrom = 0.8f)
                assert { isAtTop() }
            }
        }
    }

    @Test
    fun `scrollbar selection mode - Full`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Full,
            thumbMinHeight = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.05f)
            assert { isAtBottom() }
            moveScrollbarToTop(startFrom = 0.5f)
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.95f)
            assert { isAtBottom() }
        }
    }

    @Test
    fun `scrollbar selection actionable - Always`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Full,
            selectionActionable = ScrollbarSelectionActionable.Always,
            thumbMinHeight = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.05f)
            assert { isAtBottom() }
            moveScrollbarToTop(startFrom = 0.5f)
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.95f)
            assert { isAtBottom() }
        }
    }

    fun `scrollbar selection actionable - WhenVisible`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Full,
            selectionActionable = ScrollbarSelectionActionable.WhenVisible,
            thumbMinHeight = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.05f)
            assert { isAtTop() }
            moveScrollbarToTop(startFrom = 0.5f)
            assert { isAtTop() }
            moveScrollbarToBottom(startFrom = 0.95f)
            assert { isAtBottom() }
        }
    }

    @Composable
    private fun IndicatorContent(value: Int) {
        Surface {
            Text(
                text = "Indicator",
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    private fun setContent(
        lazyGridState: LazyGridState = LazyGridState(),
        rightSide: Boolean = true,
        alwaysShowScrollBar: Boolean = false,
        thickness: Dp = 6.dp,
        padding: Dp = 8.dp,
        thumbMinHeight: Float = 0.1f,
        thumbColor: Color = Color(0xFF2A59B6),
        thumbSelectedColor: Color = Color(0xFF5281CA),
        thumbShape: Shape = CircleShape,
        enabled: Boolean = true,
        selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
        selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
        indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
        gridItemsCount: Int = itemCount,
        reverseLayout: Boolean = false
    ) {
        composeRule.setContent {
            LazyGridVerticalScrollbar(
                state = lazyGridState,
                rightSide = rightSide,
                alwaysShowScrollBar = alwaysShowScrollBar,
                thickness = thickness,
                padding = padding,
                enabled = enabled,
                thumbMinHeight = thumbMinHeight,
                thumbColor = thumbColor,
                thumbSelectedColor = thumbSelectedColor,
                thumbShape = thumbShape,
                indicatorContent = indicatorContent,
                selectionMode = selectionMode,
                selectionActionable = selectionActionable,
            ) {
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    reverseLayout = reverseLayout
                ) {
                    items(gridItemsCount, key = { it }) {
                        Surface(
                            elevation = 3.dp,
                            modifier = Modifier.aspectRatio(1f),
                            color = Color.Yellow
                        ) {

                        }
                    }
                }
            }
        }
    }

    private fun itemTestTag(index: Int) = "Item $index"
}