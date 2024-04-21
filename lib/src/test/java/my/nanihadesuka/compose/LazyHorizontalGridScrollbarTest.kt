package my.nanihadesuka.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class LazyHorizontalGridScrollbarTest(private val itemCount: Int) {

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
    fun `scrollbar is at bottom side`() {
        setContent(side = ScrollbarLayoutSide.End)
        scrollbarScreen(composeRule) {
            assert { isScrollbarAtBottomSide() }
        }
    }

    @Test
    fun `scrollbar is at top side`() {
        setContent(side = ScrollbarLayoutSide.Start)
        scrollbarScreen(composeRule) {
            assert { isScrollbarAtTopSide() }
        }
    }

    @Test
    fun `scrollbar is at bottom side - with indicator`() {
        setContent(
            side = ScrollbarLayoutSide.End,
            indicatorContent = { value, _ -> IndicatorContent(value) }
        )
        scrollbarScreen(composeRule) {
            assert {
                isScrollbarAtBottomSide(indicatorVisible = true)
            }
        }
    }

    @Test
    fun `scrollbar is at top side - with indicator`() {
        setContent(
            side = ScrollbarLayoutSide.Start,
            indicatorContent = { value, _ -> IndicatorContent(value) }
        )
        scrollbarScreen(composeRule) {
            assert {
                isScrollbarAtTopSide(indicatorVisible = true)
            }
        }
    }

    @Test
    fun `move thumb to the left - with reverse layout`() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            moveThumbToLeft(1f)
            assert {
                isThumbAtLeft()
                isItemVisible(itemTag = itemTestTag(itemCount - 1))
            }
        }
    }

    @Test
    fun `move thumb to the right - with reverse layout`() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            moveThumbToRight()
            assert {
                isThumbAtRight()
                isItemVisible(itemTag = itemTestTag(0))
            }
        }
    }

    @Test
    fun `move thumb to the left`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            moveThumbToRight()
            assert { isThumbAtRight() }
            moveThumbToLeft(1f)
            assert {
                isThumbAtLeft()
                isItemVisible(itemTag = itemTestTag(0))
            }
        }
    }

    @Test
    fun `move thumb to the right`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            moveThumbToLeft()
            assert { isThumbAtLeft() }
            moveThumbToRight()
            assert {
                isThumbAtRight()
                isItemVisible(itemTag = itemTestTag(itemCount - 1))
            }
        }
    }

    @Test
    fun `move thumb to the right - with indicator`() {
        if (itemCount == 0) return

        setContent(indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            moveThumbToRight(startFrom = 0.05f)
            assert { isThumbAtRight(indicatorVisible = true) }
        }
    }

    @Test
    fun `move thumb to the left - with indicator`() {
        if (itemCount == 0) return

        setContent(indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            moveThumbToRight()
            assert { isThumbAtRight(indicatorVisible = true) }
            moveThumbToLeft(1f)
            assert { isThumbAtLeft(indicatorVisible = true) }
        }
    }

    @Test
    fun `always show scrollbar false`() {
        if (itemCount == 0) return

        setContent(
            alwaysShowScrollbar = false,
        )

        scrollbarScreen(composeRule) {
            // not visible without scrolling
            assert { isItemHidden(TestTagsScrollbar.scrollbarThumb) }
        }
    }

    @Test
    fun `always show scrollbar true`() {
        if (itemCount == 0) return

        setContent(
            alwaysShowScrollbar = true,
        )

        scrollbarScreen(composeRule) {
            // always visible without scrolling
            assert { isItemVisible(TestTagsScrollbar.scrollbarThumb) }
        }
    }

    @Test
    fun `scroll list to the right`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isThumbAtRight() }
        }
    }

    @Test
    fun `scroll list to the left`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isThumbAtRight() }
            scrollListToItem(testTag = itemTestTag(0))
            assert { isThumbAtLeft() }
        }
    }

    @Test
    fun `scroll list to the right - with reverse layout`() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isThumbAtLeft() }
            scrollListToItem(testTag = itemTestTag(0))
            assert { isThumbAtRight() }
        }
    }

    @Test
    fun `scroll list to the left - with reverse layout `() {
        if (itemCount == 0) return

        setContent(reverseLayout = true)
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isThumbAtLeft() }
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
    fun `correct thumb padding`() {
        setContent(padding = 10.dp)
        scrollbarScreen(composeRule) {
            assert {
                hasThumbVerticalPadding(10.dp)
            }
        }
    }

    @Test
    fun `correct thumb thickness`() {
        setContent(thickness = 7.dp)
        scrollbarScreen(composeRule) {
            assert {
                hasThumbVerticalThickness(7.dp)
            }
        }
    }

    @Test
    fun `scrollbar thumb min width`() {
        setContent(thumbMinLength = 0.2f)
        scrollbarScreen(composeRule) {
            assert {
                hasThumbMinWidthOrGreater(minValue = 0.2f)
            }
        }
    }

    @Test
    fun `scrollbar selection mode - Disabled`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Disabled,
            thumbMinLength = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.05f)
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.5f)
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.95f)
            assert { isThumbAtLeft() }
        }
    }

    @Test
    fun `scrollbar selection mode - Thumb`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Thumb,
            thumbMinLength = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert {
                isThumbAtLeft()
                hasThumbMinWidthOrGreater(0.1f)
            }
            moveThumbToRight(startFrom = 0.05f)
            assert { isThumbAtRight() }
            moveThumbToLeft(startFrom = 0.95f)
            assert { isThumbAtLeft() }

            // Now try select outside thumb area
            if (getThumbWidth() < 0.11f) {
                moveThumbToRight(startFrom = 0.2f)
                assert { isThumbAtLeft() }
                moveThumbToRight(startFrom = 0.6f)
                assert { isThumbAtLeft() }
            } else if (getThumbWidth() < 0.4f) {
                moveThumbToRight(startFrom = 0.5f)
                assert { isThumbAtLeft() }
                moveThumbToRight(startFrom = 0.8f)
                assert { isThumbAtLeft() }
            } else if (getThumbWidth() < 0.6f) {
                moveThumbToRight(startFrom = 0.7f)
                assert { isThumbAtLeft() }
                moveThumbToRight(startFrom = 0.8f)
                assert { isThumbAtLeft() }
            }
        }
    }

    @Test
    fun `scrollbar selection mode - Full`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Full,
            thumbMinLength = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.05f)
            assert { isThumbAtRight() }
            moveThumbToLeft(startFrom = 0.5f)
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.95f)
            assert { isThumbAtRight() }
        }
    }

    @Test
    fun `scrollbar selection actionable - Always`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Full,
            selectionActionable = ScrollbarSelectionActionable.Always,
            thumbMinLength = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.05f)
            assert { isThumbAtRight() }
            moveThumbToLeft(startFrom = 0.5f)
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.95f)
            assert { isThumbAtRight() }
        }
    }

    fun `scrollbar selection actionable - WhenVisible`() {
        if (itemCount == 0) return

        setContent(
            selectionMode = ScrollbarSelectionMode.Full,
            selectionActionable = ScrollbarSelectionActionable.WhenVisible,
            thumbMinLength = 0.1f,
        )
        scrollbarScreen(composeRule) {
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.05f)
            assert { isThumbAtLeft() }
            moveThumbToLeft(startFrom = 0.5f)
            assert { isThumbAtLeft() }
            moveThumbToRight(startFrom = 0.95f)
            assert { isThumbAtRight() }
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
        side: ScrollbarLayoutSide = ScrollbarLayoutSide.End,
        alwaysShowScrollbar: Boolean = true,
        thickness: Dp = 6.dp,
        padding: Dp = 8.dp,
        thumbMinLength: Float = 0.1f,
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
            LazyHorizontalGridScrollbar(
                state = lazyGridState,
                settings = ScrollbarSettings(
                    enabled = enabled,
                    side = side,
                    alwaysShowScrollbar = alwaysShowScrollbar,
                    scrollbarPadding = padding,
                    thumbThickness = thickness,
                    thumbMinLength = thumbMinLength,
                    thumbUnselectedColor = thumbColor,
                    thumbSelectedColor = thumbSelectedColor,
                    thumbShape = thumbShape,
                    selectionMode = selectionMode,
                    selectionActionable = selectionActionable,
                ),
                indicatorContent = indicatorContent,
            ) {
                LazyHorizontalGrid(
                    state = lazyGridState,
                    rows = GridCells.Adaptive(minSize = 128.dp),
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
                            Text(
                                text = "Item $it",
                                modifier = Modifier
                                    .padding(24.dp)
                                    .testTag(itemTestTag(it))
                            )
                        }
                    }
                }
            }
        }
    }

    private fun itemTestTag(index: Int) = "Item $index"
}