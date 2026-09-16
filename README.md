[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar/month.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://github.com/nanihadesuka/LazyColumnScrollbar/actions/workflows/tests.yml/badge.svg?branch=master)](https://github.com/nanihadesuka/LazyColumnScrollbar/commits/master)
[![License](https://img.shields.io/badge/License-MIT-blue)](https://github.com/nanihadesuka/LazyColumnScrollbar/blob/main/LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-339933)
![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.12.0-339933)
![Platforms](https://img.shields.io/badge/Platforms-Android_%7C_iOS_%7C_macOS_%7C_JVM_%7C_JS_%7C_Wasm-339933)

# Scrollbars implementation for Compose Multiplatform

Compose Multiplatform implementation of the scroll bar. Can drag, scroll smoothly and includes animations.

### Features:

- Support for:
    - Column, Row, LazyColumn, LazyRow, LazyVerticalGrid, LazyHorizontalGrid, LazyVerticalStaggeredGrid, LazyHorizontalStaggeredGrid
- Kotlin Multiplatform targets:
    - Android, iOS (arm64, simulatorArm64), macOS (arm64), JVM desktop, JS, Wasm
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

Add the JitPack repository in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

Add the dependency to `commonMain` in a multiplatform module:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.nanihadesuka.LazyColumnScrollbar:lazycolumnscrollbar:3.0.0")
        }
    }
}
```

Or to an Android-only module:

```kotlin
dependencies {
    implementation("com.github.nanihadesuka.LazyColumnScrollbar:lazycolumnscrollbar:3.0.0")
}
```

> **Migrating from 2.x:** the coordinates changed from `com.github.nanihadesuka:LazyColumnScrollbar`
> to `com.github.nanihadesuka.LazyColumnScrollbar:lazycolumnscrollbar`. Package names are unchanged.

# Usage

Every scrollbar wraps its scrollable content and takes the same state object as that content:

| Scrollbar | State |
|---|---|
| `ColumnScrollbar`, `RowScrollbar` | `rememberScrollState()` |
| `LazyColumnScrollbar`, `LazyRowScrollbar` | `rememberLazyListState()` |
| `LazyVerticalGridScrollbar`, `LazyHorizontalGridScrollbar` | `rememberLazyGridState()` |
| `LazyVerticalStaggeredGridScrollbar`, `LazyHorizontalStaggeredGridScrollbar` | `rememberLazyStaggeredGridState()` |

All of them also accept an optional `modifier`, `settings` (see [Default settings parameters](#default-settings-parameters)) and `indicatorContent`.

### LazyColumn

```kotlin
val listData = (0..1000).toList()
val listState = rememberLazyListState()

LazyColumnScrollbar(
    state = listState,
    settings = ScrollbarSettings.Default,
) {
    LazyColumn(state = listState) {
        items(listData) {
            Text("Item $it")
        }
    }
}
```

### Column

```kotlin
val scrollState = rememberScrollState()

ColumnScrollbar(state = scrollState) {
    Column(Modifier.verticalScroll(scrollState)) {
        repeat(100) {
            Text("Item $it")
        }
    }
}
```

### LazyVerticalStaggeredGrid

Staggered grid scrollbars take an extra `reverseLayout` parameter, which must match the grid's own `reverseLayout`:

```kotlin
val gridState = rememberLazyStaggeredGridState()
val reverseLayout = false

LazyVerticalStaggeredGridScrollbar(
    state = gridState,
    reverseLayout = reverseLayout,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = gridState,
        reverseLayout = reverseLayout,
    ) {
        items(100) {
            Text("Item $it", Modifier.height((40 + it % 5 * 20).dp))
        }
    }
}
```

### Position indicator

`indicatorContent` shows a composable next to the thumb while scrolling.

For lazy scrollbars it receives the first visible item `index`:

```kotlin
indicatorContent = { index, isThumbSelected ->
    Text(
        text = "i: $index",
        Modifier.background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
    )
}
```

For `ColumnScrollbar` and `RowScrollbar` it receives a `normalizedOffset` between `0f` and `1f` instead:

```kotlin
indicatorContent = { normalizedOffset, isThumbSelected ->
    Text(
        text = "${(normalizedOffset * 100).roundToInt()}%",
        Modifier.background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
    )
}
```

### Placing the scrollbar independently

Each scrollbar also has an `Internal*` variant (e.g. `InternalLazyColumnScrollbar`) that draws only the scrollbar, so it can be placed anywhere in your layout instead of wrapping the content.

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
