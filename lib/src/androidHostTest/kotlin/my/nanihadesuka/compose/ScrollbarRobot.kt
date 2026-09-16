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
    fun moveThumbToTop(startFrom: Float = 1f) = composeRule
        .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
        .performTouchInput {
            swipeUp(startY = top * (1f - startFrom) + bottom * startFrom)
        }

    /**
     * @param startFrom 0f is top 1f is bottom
     */
    fun moveThumbToBottom(startFrom: Float = 0f) = composeRule
        .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
        .performTouchInput {
            swipeDown(startY = top * (1f - startFrom) + bottom * startFrom)
        }

    /**
     * @param startFrom 0f is left 1f is right
     */
    fun moveThumbToRight(startFrom: Float = 0f) = composeRule
        .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
        .performTouchInput {
            swipeRight(startX = left * (1f - startFrom) + right * startFrom)
        }

    /**
     * @param startFrom 0f is left 1f is right
     */
    fun moveThumbToLeft(startFrom: Float = 0f) = composeRule
        .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
        .performTouchInput {
            swipeLeft(startX = left * (1f - startFrom) + right * startFrom)
        }

    fun scrollListToItem(testTag: String) = composeRule
        .onNode(hasScrollAction())
        .performScrollToNode(hasTestTag(testTag))

    /**
     * Normalized thumb height compared against parent container
     */
    fun getThumbHeight() = thumbBounds.height.value / scrollbarContainerBounds.height.value

    /**
     * Normalized thumb height compared against parent container
     */
    fun getThumbWidth() = thumbBounds.width.value / scrollbarContainerBounds.width.value

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

        fun isThumbAtTop(indicatorVisible: Boolean = false, tolerance: Dp = 2.dp) {
            assertEqualWithTolerance(
                thumbBounds.top,
                scrollbarContainerBounds.top,
                tolerance = tolerance
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(
                    indicatorBounds.verticalCenter,
                    thumbBounds.verticalCenter
                )
            } else {
                indicatorNotExist()
            }
        }

        fun isThumbAtLeft(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(thumbBounds.left, scrollbarContainerBounds.left)
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(
                    indicatorBounds.horizontalCenter,
                    thumbBounds.horizontalCenter
                )
            } else {
                indicatorNotExist()
            }
        }

        fun isThumbAtBottom(indicatorVisible: Boolean = false, tolerance: Dp = 2.dp) {
            assertEqualWithTolerance(
                thumbBounds.bottom,
                scrollbarContainerBounds.bottom,
                tolerance = tolerance
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(
                    indicatorBounds.verticalCenter,
                    thumbBounds.verticalCenter
                )
            } else {
                indicatorNotExist()
            }
        }

        fun isThumbAtRight(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(
                thumbBounds.right,
                scrollbarContainerBounds.right,
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(
                    indicatorBounds.horizontalCenter,
                    thumbBounds.horizontalCenter
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

        fun hasThumbHorizontalThickness(value: Dp) {
            assertEqualWithTolerance(thumbBounds.width, value, tolerance = 0.1.dp)
        }

        fun hasThumbVerticalThickness(value: Dp) {
            assertEqualWithTolerance(thumbBounds.height, value, tolerance = 0.1.dp)
        }

        fun hasThumbHorizontalPadding(value: Dp) {
            assertEqualWithTolerance(thumbPaddingHorizontal, value, tolerance = 0.1.dp)
        }

        fun hasThumbVerticalPadding(value: Dp) {
            assertEqualWithTolerance(thumbPaddingVertical, value, tolerance = 0.1.dp)
        }

        fun hasThumbHeight(value: Float) {
            assertEqualWithTolerance(
                thumbBounds.height.value / scrollbarContainerBounds.height.value,
                value,
                tolerance = 0.01f
            )
        }

        fun hasThumbWidth(value: Float) {
            assertEqualWithTolerance(
                thumbBounds.width.value / scrollbarContainerBounds.width.value,
                value,
                tolerance = 0.01f
            )
        }

        fun hasThumbMinHeightOrGreater(minValue: Float) {
            assertEqualOrGreater(
                thumbBounds.height.value / scrollbarContainerBounds.height.value,
                minValue,
                tolerance = 0.01f
            )
        }

        fun hasThumbMinWidthOrGreater(minValue: Float) {
            assertEqualOrGreater(
                thumbBounds.width.value / scrollbarContainerBounds.width.value,
                minValue,
                tolerance = 0.01f
            )
        }

        fun isScrollbarAtLeftSide(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(
                thumbBounds.left - thumbPaddingHorizontal,
                containerBounds.left
            )
            assertEqualWithTolerance(
                scrollbarContainerBounds.left,
                containerBounds.left
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(indicatorBounds.left, thumbBounds.right)
            } else {
                indicatorNotExist()
            }
        }

        fun isScrollbarAtRightSide(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(
                thumbBounds.right + thumbPaddingHorizontal,
                containerBounds.right
            )
            assertEqualWithTolerance(
                scrollbarContainerBounds.right,
                containerBounds.right
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(indicatorBounds.right, thumbBounds.left)
            } else {
                indicatorNotExist()
            }
        }

        fun isScrollbarAtTopSide(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(
                thumbBounds.top - thumbPaddingVertical,
                containerBounds.top
            )
            assertEqualWithTolerance(
                scrollbarContainerBounds.top,
                containerBounds.top
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(indicatorBounds.top, thumbBounds.bottom)
            } else {
                indicatorNotExist()
            }
        }

        fun isScrollbarAtBottomSide(indicatorVisible: Boolean = false) {
            assertEqualWithTolerance(
                thumbBounds.bottom + thumbPaddingVertical,
                containerBounds.bottom
            )
            assertEqualWithTolerance(
                scrollbarContainerBounds.bottom,
                containerBounds.bottom
            )
            if (indicatorVisible) {
                indicatorExist()
                assertEqualWithTolerance(indicatorBounds.bottom, thumbBounds.top)
            } else {
                indicatorNotExist()
            }
        }
    }

    private val thumbPaddingHorizontal
        get() = (scrollbarContainerBounds.width - thumbBounds.width) / 2

    private val thumbPaddingVertical
        get() = (scrollbarContainerBounds.height - thumbBounds.height) / 2

    private val containerBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.container)
            .getUnclippedBoundsInRoot()

    private val indicatorBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarIndicator)
            .getUnclippedBoundsInRoot()

    private val thumbBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarThumb)
            .getUnclippedBoundsInRoot()

    private val scrollbarContainerBounds
        get() = composeRule
            .onNodeWithTag(TestTagsScrollbar.scrollbarContainer)
            .getUnclippedBoundsInRoot()
}


private val DpRect.verticalCenter: Dp get() = (bottom + top) / 2
private val DpRect.horizontalCenter: Dp get() = (right + left) / 2

private fun assertEqualWithTolerance(valueA: Float, valueB: Float, tolerance: Float = 0.01f) {
    assert((valueA - valueB).absoluteValue <= tolerance) {
        "Not equal: valueA=$valueA valueB=$valueB tolerance=$tolerance"
    }
}

private fun assertEqualWithTolerance(valueA: Dp, valueB: Dp, tolerance: Dp = 2.dp) {
    assert((valueA - valueB).value.absoluteValue <= tolerance.value) {
        "Not equal: valueA=$valueA valueB=$valueB tolerance=$tolerance"
    }
}

private fun assertEqualOrGreater(value: Float, minValue: Float, tolerance: Float) {
    assert(value >= minValue - tolerance) {
        "Not equal or grater: value=$value minValue=$minValue"
    }
}