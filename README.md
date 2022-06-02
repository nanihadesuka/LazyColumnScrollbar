[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)
[![](https://jitpack.io/v/nanihadesuka/LazyColumnScrollbar/month.svg)](https://jitpack.io/#nanihadesuka/LazyColumnScrollbar)


# LazyColumn scrollbar compose library

Compose implementation of the scroll bar. Can drag, scroll smoothly and includes animations.

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
        implementation 'com.github.nanihadesuka:LazyColumnScrollbar:1.1.0'
    }
```

## How to use

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
```
indicatorContent = { index, isThumbSelected ->
    Text(
        text = "i: $index",
        Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 16.dp
                )
            )
            .background(Color.Green)
            .padding(8.dp)
            .clip(CircleShape)
            .background(if (isThumbSelected) Color.Red else Color.Black)
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
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
)
```
