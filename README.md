[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar/month.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://github.com/nanihadesuka/LazyColumnScrollbar/actions/workflows/default.yml/badge.svg?branch=master)](https://github.com/nanihadesuka/LazyColumnScrollbar/commits/master)

# LazyColumn && Column scrollbar for jetpack compose

Compose implementation of the scroll bar. Can drag, scroll smoothly and includes animations.

### Features:
- Support for LazyColumn's sticky headers
- Support for LazyColumn's reverseLayout
- Optional current position indicator
- Multiple selection states (Disabled, Full, Thumb)
- Multiple selection actionable states (Always, WhenVisible)
- Customizable look
- Easy integration with other composables
- UI tests

## Installation

Add it in your root build.gradle at the end of  repositories:

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
        implementation 'com.github.nanihadesuka:LazyColumnScrollbar:1.7.2'
}
```

# How to use for lazyColumn

Simply wrap the LazyColumn with it

```kotlin
val listData = (0..1000).toList()
val listState = rememberLazyListState()

LazyColumnScrollbar(listState) {
    LazyColumn(state = listState) {
        items(listData) {
            Text(
                text = "Item $it",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            )
        }
    }
}
```

indicatorContent example:
```kotlin
indicatorContent = { index, isThumbSelected ->
    Text(
        text = "i: $index",
        Modifier
            .padding(4.dp)
            .background(if (isThumbSelected) Color.Red else Color.Black, CircleShape)
            .padding(12.dp)
    )
}
```

## LazyColumnScrollbar arguments

```kotlin
fun LazyColumnScrollbar(
    listState: LazyListState,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    enabled: Boolean = true,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
)
```

# How to use for Column
Simply wrap the LazyColumn with it

```kotlin
val listData = (0..1000).toList()
val listState = rememberLazyListState()

ColumnScrollbar(listState) {
    Column(
        modifier = Modifier.verticalScroll(listState)
    ) {
        for (it in listData) {
            Text(
                text = "Item $it",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
```

## ColumnScrollbar arguments

```kotlin
fun ColumnScrollbar(
    state: ScrollState,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    enabled: Boolean = true,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
)
```

# License
Copyright © 2022, [nani](https://github.com/nanihadesuka), Released under [MIT License](LICENSE)
