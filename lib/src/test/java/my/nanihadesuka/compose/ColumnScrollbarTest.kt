package my.nanihadesuka.compose


import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
class ColumnScrollbarTest(private val itemCount: Int) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = " Items count: {0} ")
        fun parametrizeListItemCount() = listOf<Array<Int>>(
            arrayOf(0),
            arrayOf(1),
            arrayOf(10),
            arrayOf(100)
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
        setContent(side = ScrollbarLayoutSide.End)
        scrollbarScreen(composeRule) {
            assert { isScrollbarAtRightSide() }
        }
    }

    @Test
    fun `scrollbar is at left side`() {
        setContent(side = ScrollbarLayoutSide.Start)
        scrollbarScreen(composeRule) {
            assert { isScrollbarAtLeftSide() }
        }
    }

    @Test
    fun `scrollbar is at right side - with indicator`() {
        setContent(
            side = ScrollbarLayoutSide.End,
            indicatorContent = { value, _ -> IndicatorContent(value) }
        )
        scrollbarScreen(composeRule) {
            assert {
                isScrollbarAtRightSide(indicatorVisible = true)
            }
        }
    }

    @Test
    fun `scrollbar is at left side - with indicator`() {
        setContent(
            side = ScrollbarLayoutSide.Start,
            indicatorContent = { value, _ -> IndicatorContent(value) }
        )
        scrollbarScreen(composeRule) {
            assert {
                isScrollbarAtLeftSide(indicatorVisible = true)
            }
        }
    }

    @Test
    fun `move scrollbar to the bottom`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            moveThumbToBottom()
            assert { isThumbAtBottom() }
        }
    }

    @Test
    fun `move scrollbar to the top`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            moveThumbToBottom()
            assert { isThumbAtBottom() }
            moveThumbToTop()
            assert { isThumbAtTop() }
        }
    }

    @Test
    fun `move scrollbar to the bottom - with indicator`() {
        if (itemCount == 0) return

        setContent(indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            moveThumbToBottom()
            assert { isThumbAtBottom(indicatorVisible = true) }
        }
    }

    @Test
    fun `move scrollbar to the top - with indicator`() {
        if (itemCount == 0) return

        setContent(indicatorContent = { value, _ -> IndicatorContent(value) })
        scrollbarScreen(composeRule) {
            moveThumbToBottom()
            assert { isThumbAtBottom(indicatorVisible = true) }
            moveThumbToTop()
            assert { isThumbAtTop(indicatorVisible = true) }
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
            assert { isItemHidden(TestTagsScrollbar.scrollbarThumb) }
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
            assert { isItemVisible(TestTagsScrollbar.scrollbarThumb) }
        }
    }

    @Test
    fun `scroll list to the bottom`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isThumbAtBottom() }
        }
    }

    @Test
    fun `scroll list to the top`() {
        if (itemCount == 0) return

        setContent()
        scrollbarScreen(composeRule) {
            scrollListToItem(testTag = itemTestTag(itemCount - 1))
            assert { isThumbAtBottom() }
            scrollListToItem(testTag = itemTestTag(0))
            assert { isThumbAtTop() }
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
                hasThumbHorizontalPadding(10.dp)
            }
        }
    }

    @Test
    fun `correct scrollbar thickness`() {
        setContent(thickness = 7.dp)
        scrollbarScreen(composeRule) {
            assert {
                hasThumbHorizontalThickness(7.dp)
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
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.05f)
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.5f)
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.95f)
            assert { isThumbAtTop() }
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
                isThumbAtTop()
                hasThumbMinHeightOrGreater(0.1f)
            }
            moveThumbToBottom(startFrom = 0.05f)
            assert { isThumbAtBottom() }
            moveThumbToTop(startFrom = 0.95f)
            assert { isThumbAtTop() }

            // Now try select outside thumb area
            if (getThumbHeight() < 0.11f) {
                moveThumbToBottom(startFrom = 0.2f)
                assert { isThumbAtTop() }
                moveThumbToBottom(startFrom = 0.6f)
                assert { isThumbAtTop() }
            } else if (getThumbHeight() < 0.4f) {
                moveThumbToBottom(startFrom = 0.5f)
                assert { isThumbAtTop() }
                moveThumbToBottom(startFrom = 0.8f)
                assert { isThumbAtTop() }
            } else if (getThumbHeight() < 0.6f) {
                moveThumbToBottom(startFrom = 0.7f)
                assert { isThumbAtTop() }
                moveThumbToBottom(startFrom = 0.8f)
                assert { isThumbAtTop() }
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
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.05f)
            assert { isThumbAtBottom() }
            moveThumbToTop(startFrom = 0.5f)
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.95f)
            assert { isThumbAtBottom() }
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
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.05f)
            assert { isThumbAtBottom() }
            moveThumbToTop(startFrom = 0.5f)
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.95f)
            assert { isThumbAtBottom() }
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
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.05f)
            assert { isThumbAtTop() }
            moveThumbToTop(startFrom = 0.5f)
            assert { isThumbAtTop() }
            moveThumbToBottom(startFrom = 0.95f)
            assert { isThumbAtBottom() }
        }
    }

    @Composable
    private fun IndicatorContent(value: Float) {
        Surface {
            Text(
                text = "Indicator",
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    private fun setContent(
        state: ScrollState = ScrollState(initial = 0),
        side: ScrollbarLayoutSide = ScrollbarLayoutSide.End,
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
        indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
        listItemsCount: Int = itemCount
    ) {
        composeRule.setContent {
            ColumnScrollbar(
                state = state,
                side = side,
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
                Column(Modifier.verticalScroll(state = state)) {
                    repeat(listItemsCount) {
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

    private fun itemTestTag(index: Int) = "Item $index"
}