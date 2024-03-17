package my.nanihadesuka.lazycolumnscrollbar

import android.os.Bundle
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.ColumnScrollbar
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.LazyGridVerticalScrollbar
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.lazycolumnscrollbar.ui.theme.LazyColumnScrollbarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainView() }
    }
}

enum class TypeTab {
    LazyColumn, Column, LazyGrid
}

@Preview(showBackground = true)
@Composable
fun MainView() {
    LazyColumnScrollbarTheme {
        Surface(color = MaterialTheme.colors.background) {
            val tab = rememberSaveable { mutableStateOf(TypeTab.LazyColumn) }
            Column {
                Row {
                    Text(
                        text = TypeTab.LazyColumn.name,
                        Modifier.selectable(
                            selected = tab.value == TypeTab.LazyColumn,
                            onClick = { tab.value = TypeTab.LazyColumn },
                        )
                    )
                    Text(
                        text = TypeTab.Column.name,
                        Modifier.selectable(
                            selected = tab.value == TypeTab.Column,
                            onClick = { tab.value = TypeTab.Column },
                        )
                    )
                    Text(
                        text = TypeTab.LazyGrid.name,
                        Modifier.selectable(
                            selected = tab.value == TypeTab.LazyGrid,
                            onClick = { tab.value = TypeTab.LazyGrid },
                        )
                    )
                }

                when (tab.value) {
                    TypeTab.LazyColumn -> LazyColumnView()
                    TypeTab.Column -> ColumnView()
                    TypeTab.LazyGrid -> LazyGridView()
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyColumnView() {
    val listData = (0..1000).toList()
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .padding(16.dp)
            .border(width = 1.dp, MaterialTheme.colors.primary)
            .padding(1.dp)
    ) {
        LazyColumnScrollbar(
            listState,
            selectionMode = ScrollbarSelectionMode.Thumb,
            alwaysShowScrollBar = true,
            indicatorContent = { index, isThumbSelected ->
                Surface {
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
                            .background(if (isThumbSelected) MaterialTheme.colors.surface else MaterialTheme.colors.background)
                            .padding(12.dp)
                    )
                }
            }
        ) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 300.dp)
            ) {
                (0..3).forEach { number ->
                    stickyHeader {
                        Surface {
                            Text(
                                text = "HEADER $number",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                items(listData) {
                    Text(
                        text = "Item $it",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .padding(vertical = 30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LazyGridView() {
    val photos by rememberSaveable {
        mutableStateOf(List(100) { it })
    }

    val lazyGridState = rememberLazyGridState()
    Box(
        modifier = Modifier
            .padding(16.dp)
            .border(width = 1.dp, MaterialTheme.colors.primary)
            .padding(1.dp)
    ) {
        LazyGridVerticalScrollbar(
            state = lazyGridState,
            selectionMode = ScrollbarSelectionMode.Thumb,
            alwaysShowScrollBar = true,
            indicatorContent = { index, isThumbSelected ->
                Surface {
                    Text(
                        text = "i: $index",
                        modifier = Modifier
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
                            .background(if (isThumbSelected) Color.Blue else Color.Yellow)
                            .padding(12.dp),
                        color = Color.Red
                    )
                }
            }
        ) {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(minSize = 128.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(photos.size, key = { it }) {
                    Surface(
                        elevation = 3.dp,
                        modifier = Modifier.aspectRatio(1f),
                        color = Color.Yellow
                    ) {
                        Text(
                            text = "Item $it",
                            modifier = Modifier
                                .padding(24.dp),
                            color = Color.Black
                        )

                    }
                }
            }
        }

    }
}

@Composable
fun ColumnView() {
    val listData = (0..100).toList()
    val listState = rememberScrollState()
    val indicatorContent = @Composable { normalizedOffset: Float, isThumbSelected: Boolean ->
        Surface {
            Text(
                text = "i: ${"%.2f".format(normalizedOffset)}",
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
                    .background(if (isThumbSelected) MaterialTheme.colors.surface else MaterialTheme.colors.background)
                    .padding(12.dp)
            )
        }
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .border(width = 1.dp, MaterialTheme.colors.primary)
            .padding(1.dp)
    ) {
        ColumnScrollbar(
            state = listState,
            indicatorContent = indicatorContent,
            selectionMode = ScrollbarSelectionMode.Disabled,
            alwaysShowScrollBar = true,
        ) {
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
    }
}
