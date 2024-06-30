[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar/month.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://github.com/nanihadesuka/LazyColumnScrollbar/actions/workflows/tests.yml/badge.svg?branch=master)](https://github.com/nanihadesuka/LazyColumnScrollbar/commits/master)
[![License](https://img.shields.io/badge/License-MIT-blue)](https://github.com/nanihadesuka/LazyColumnScrollbar/blob/main/LICENSE)
![Foo23 - Bar](https://img.shields.io/badge/Kotlin-1.9.23-339933)
![Foo23 - Bar](https://img.shields.io/badge/Compose_BOM-2024.04.00-339933)

# Scrollbars implementation for jetpack compose

Compose implementation of the scroll bar. Can drag, scroll smoothly and includes animations.

### Features:

- Support for:
    - Column, Row, LazyColumn, LazyRow, LazyVerticalGrid, LazyHorizontalGrid
- Takes into account:
    - sticky headers
    - reverseLayout
- Optional current position indicator
- Multiple selection modes:
    - States (Disabled, Full, Thumb)
    - Actionable states (Always, WhenVisible)
- Customizable look
- Easy integration with other composables
- Extensive UI tests
- Sample app

## Installation

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add it to your app build.gradle

```groovy
dependencies {
    implementation 'com.github.nanihadesuka:LazyColumnScrollbar:2.2.0'
}
```

# Available scrolls components
- ColumnScrollbar
- RowScrollbar
- LazyColumnScrollbar
- LazyRowScrollbar
- LazyVerticalGridScrollbar
- LazyHorizontalGridScrollbar

# Example for LazyColumn
```kotlin
val listData = (0..1000).toList()
val listState = rememberLazyListState()

LazyColumnScrollbar(
  state = listState,
  settings = ScrollbarSettings.Default  
) {
    LazyColumn(state = listState) {
        items(listData) {
            Text("Item $it")
        }
    }
}
```

indicatorContent example:

```kotlin
indicatorContent = { index, isThumbSelected ->
    Text(
        text = "i: $index",
        Modifier.background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
    )
}
```

# Default settings parameters
```kotlin
/**
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 */
@Stable
data class ScrollbarSettings(
  val enabled: Boolean = Default.enabled,
  val side: ScrollbarLayoutSide = Default.side,
  val alwaysShowScrollbar: Boolean = Default.alwaysShowScrollbar,
  val scrollbarPadding: Dp = Default.scrollbarPadding,
  val thumbThickness: Dp = Default.thumbThickness,
  val thumbShape: Shape = Default.thumbShape,
  val thumbMinLength: Float = Default.thumbMinLength,
  val thumbMaxLength: Float = Default.thumbMaxLength,
  val thumbUnselectedColor: Color = Default.thumbUnselectedColor,
  val thumbSelectedColor: Color = Default.thumbSelectedColor,
  val selectionMode: ScrollbarSelectionMode = Default.selectionMode,
  val selectionActionable: ScrollbarSelectionActionable = Default.selectionActionable,
  val hideDelayMillis: Int = Default.hideDelayMillis,
  val hideDisplacement: Dp = Default.hideDisplacement,
  val hideEasingAnimation: Easing = Default.hideEasingAnimation,
  val durationAnimationMillis: Int = Default.durationAnimationMillis,
) {
  init {
    require(thumbMinLength <= thumbMaxLength) {
      "thumbMinLength ($thumbMinLength) must be less or equal to thumbMaxLength ($thumbMaxLength)"
    }
  }

  companion object {
    val Default = ScrollbarSettings(
      enabled = true,
      side = ScrollbarLayoutSide.End,
      alwaysShowScrollbar = false,
      thumbThickness = 6.dp,
      scrollbarPadding = 8.dp,
      thumbMinLength = 0.1f,
      thumbMaxLength = 1.0f,
      thumbUnselectedColor = Color(0xFF2A59B6),
      thumbSelectedColor = Color(0xFF5281CA),
      thumbShape = CircleShape,
      selectionMode = ScrollbarSelectionMode.Thumb,
      selectionActionable = ScrollbarSelectionActionable.Always,
      hideDelayMillis = 400,
      hideDisplacement = 14.dp,
      hideEasingAnimation = FastOutSlowInEasing,
      durationAnimationMillis = 500,
    )
  }
}
```

# License

Copyright © 2024, [nani](https://github.com/nanihadesuka), Released under [MIT License](LICENSE)
