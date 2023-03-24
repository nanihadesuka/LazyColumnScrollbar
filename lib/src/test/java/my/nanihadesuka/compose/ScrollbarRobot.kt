package my.nanihadesuka.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.*
import kotlin.math.absoluteValue

fun scrollbarScreen(
    composeRule: ComposeContentTestRule,
    actions: ScrollbarRobot.() -> Unit
) = ScrollbarRobot(composeRule).apply(actions)

class ScrollbarRobot(private val composeRule: ComposeContentTestRule) {

    /**
     * @param startFrom 0f is top 1f is bottom
     */
    fun moveScrollbarToTop(startFrom: Float = 1f) = composeRule
        .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
        .performTouchInput {
            swipeUp(startY = top * (1f - startFrom) + bottom * startFrom)
        }

    /**
     * @param startFrom 0f is top 1f is bottom
     */
    fun moveScrollbarToBottom(startFrom: Float = 0f) = composeRule
        .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
        .performTouchInput {
            swipeDown(startY = top * (1f - startFrom) + bottom * startFrom)
        }

    fun scrollListToItem(testTag: String) = composeRule
        .onNode(hasScrollAction())
        .performScrollToNode(hasTestTag(testTag))

    /**
     * Normalized thumb height compared against parent container
     */
    fun getThumbHeight() = scrollbarBounds.height.value / scrollbarContainerBounds.height.value

    /**
     * Normalized thumb bottom position compared against parent container top
     */
    fun getThumbBottomPosition(): Float {
        val base = scrollbarContainerBounds.top.value
        return (scrollbarBounds.bottom.value - base) / scrollbarContainerBounds.height.value
    }

    fun assert(assertions: Assertions.() -> Unit) = Assertions().apply(assertions)

    inner class Assertions {

        fun isEnabled() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
            .assertExists()

        fun isDisabled() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
            .assertDoesNotExist()

        fun indicatorExist() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarIndicator)
            .assertExists()

        fun indicatorNotExist() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarIndicator)
            .assertDoesNotExist()

        fun isAtTop(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(scrollbarBounds.top, scrollbarContainerBounds.top)
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(
                    indicatorBounds.verticalCenter,
                    scrollbarBounds.verticalCenter
                )
            } else {
                indicatorNotExist()
            }
        }

        fun isAtBottom(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(
                scrollbarBounds.bottom,
                scrollbarContainerBounds.bottom,
                tolerance = 15.dp
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(
                    indicatorBounds.verticalCenter,
                    scrollbarBounds.verticalCenter
                )
            } else {
                indicatorNotExist()
            }
        }

        fun isItemVisible(itemTag: String) {
            composeRule
                .onNodeWithTag(itemTag)
                .assertIsDisplayed()
        }

        fun isItemHidden(itemTag: String) {
            composeRule
                .onNodeWithTag(itemTag)
                .assertIsNotDisplayed()
        }

        fun isAtLeftSide(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(scrollbarBounds.left, 0.dp)
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(indicatorBounds.left, scrollbarBounds.right)
            } else {
                indicatorNotExist()
            }
        }

        fun hasScrollbarThickness(value: Dp) {
            assertEqualWithTolerance(scrollbarBounds.width, value, tolerance = 0.1.dp)
        }

        fun hasScrollbarPadding(value: Dp) {
            assertEqualWithTolerance(
                (scrollbarContainerBounds.width - scrollbarBounds.width) / 2,
                value,
                tolerance = 0.1.dp
            )
        }

        fun hasThumbMinHeightOrGreater(minValue: Float) {
            assertEqualOrGreater(
                scrollbarBounds.height.value / scrollbarContainerBounds.height.value,
                minValue,
                tolerance = 0.01f
            )
        }

        fun isAtRightSide(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(scrollbarBounds.right, scrollbarContainerBounds.right)
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(indicatorBounds.right, scrollbarBounds.left)
            } else {
                indicatorNotExist()
            }
        }


    }

    private val indicatorBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarIndicator)
            .getUnclippedBoundsInRoot()

    private val scrollbarBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbar)
            .getUnclippedBoundsInRoot()

    private val scrollbarContainerBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
            .getUnclippedBoundsInRoot()
}


private val DpRect.verticalCenter: Dp get() = (bottom + top) / 2

private fun assertEqualWithTolerance(valueA: Dp, valueB: Dp, tolerance: Dp = 15.dp) {
    assert((valueA - valueB).value.absoluteValue <= tolerance.value) {
        "Not equal: valueA=$valueA valueB=$valueB tolerance=$tolerance"
    }
}

private fun assertEqualOrGreater(value: Float, minValue: Float, tolerance: Float) {
    assert(value >= minValue - tolerance) {
        "Not equal or grater: value=$value minValue=$minValue"
    }
}