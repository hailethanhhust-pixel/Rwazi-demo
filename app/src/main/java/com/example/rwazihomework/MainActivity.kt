package com.example.rwazihomework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rwazihomework.domain.model.Note
import com.example.rwazihomework.ui.home.colorFromHex
import com.example.rwazihomework.ui.home.formatDateStamp
import com.example.rwazihomework.ui.home.HomeUiState
import com.example.rwazihomework.ui.home.HomeViewModel
import com.example.rwazihomework.ui.home.readableTextColor
import com.example.rwazihomework.ui.home.SortOrder
import com.example.rwazihomework.ui.theme.RwaziHomeWorkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RwaziHomeWorkTheme {
                HomeScreen(
                    viewModel = homeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.searchQuery, uiState.sortOrder) {
        if (uiState.displayedItems.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(listState, uiState.hasMoreItems, uiState.displayedItems.size) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.distinctUntilChanged().collect { lastVisibleIndex ->
            viewModel.onListScrolled(lastVisibleIndex)
        }
    }

    LaunchedEffect(uiState.displayedItems.size) {
        val lastIndex = uiState.displayedItems.lastIndex
        if (lastIndex >= 0 && listState.firstVisibleItemIndex > lastIndex) {
            listState.scrollToItem(lastIndex)
        }
    }

    HomeContent(
        uiState = uiState,
        listState = listState,
        modifier = modifier,
        onOpenAddDialog = viewModel::onOpenAddDialog,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortOrderChange = viewModel::onSortOrderChange,
        onDelete = viewModel::onDelete,
        onOpenEditDialog = viewModel::onOpenEditDialog,
        onDismissDialog = viewModel::onDismissDialog,
        onDialogInputChange = viewModel::onDialogInputChange,
        onConfirmDialog = viewModel::onConfirmDialog
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeContent(
    uiState: HomeUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
    onOpenAddDialog: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onDelete: (String) -> Unit,
    onOpenEditDialog: (Note) -> Unit,
    onDismissDialog: () -> Unit,
    onDialogInputChange: (String) -> Unit,
    onConfirmDialog: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onOpenAddDialog()
                }
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_item_content_description)
                )
            }
        }
    ) { innerPadding ->
        if (uiState.filteredItems.isEmpty() && uiState.searchQuery.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_notes_prompt),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    label = { Text(stringResource(R.string.search_notes_label)) },
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        Button(onClick = { sortMenuExpanded = true }) {
                            Text(
                                text = when (uiState.sortOrder) {
                                    SortOrder.Newest -> stringResource(R.string.sort_newest_label)
                                    SortOrder.Oldest -> stringResource(R.string.sort_oldest_label)
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.newest)) },
                                onClick = {
                                    onSortOrderChange(SortOrder.Newest)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.oldest)) },
                                onClick = {
                                    onSortOrderChange(SortOrder.Oldest)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (uiState.filteredItems.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_search_results),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.loaded_notes_count,
                            uiState.displayedItems.size,
                            uiState.filteredItems.size
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(
                        items = uiState.displayedItems,
                        key = { note -> note.id }
                    ) { note ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        var didDelete by remember(note.id) { mutableStateOf(false) }

                        LaunchedEffect(dismissState.currentValue, didDelete) {
                            if (!didDelete) {
                                when (dismissState.currentValue) {
                                    SwipeToDismissBoxValue.EndToStart,
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        didDelete = true
                                        onDelete(note.id)
                                    }

                                    SwipeToDismissBoxValue.Settled -> Unit
                                }
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = stringResource(R.string.delete), color = Color.Red)
                                }
                            }
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = note.text,
                                        maxLines = 2
                                    )
                                },
                                supportingContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.created_on,
                                                formatDateStamp(note.createdAt)
                                            )
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = colorFromHex(note.backgroundColorHex),
                                    headlineColor = readableTextColor(colorFromHex(note.backgroundColorHex)),
                                    supportingColor = readableTextColor(colorFromHex(note.backgroundColorHex))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .clickable {
                                        onOpenEditDialog(note)
                                    }
                            )
                        }
                    }

                    if (uiState.isLoadingMore) {
                        item(key = "loading-indicator") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (uiState.hasMoreItems) {
                        item(key = "load-more-hint") {
                            Text(
                                text = stringResource(R.string.scroll_to_load_more),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismissDialog()
            },
            title = {
                Text(
                    if (uiState.editingNoteId == null) {
                        stringResource(R.string.add_item_title)
                    } else {
                        stringResource(R.string.edit_item_title)
                    }
                )
            },
            text = {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { onDialogInputChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.enter_text_label)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmDialog()
                    }
                ) {
                    Text(
                        if (uiState.editingNoteId == null) {
                            stringResource(R.string.add)
                        } else {
                            stringResource(R.string.save)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissDialog()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RwaziHomeWorkTheme {
        HomeContent(
            uiState = HomeUiState(
                filteredItems = listOf(
                    Note(
                        id = "1",
                        text = stringResource(R.string.preview_note_text),
                        createdAt = System.currentTimeMillis(),
                        backgroundColorHex = "#80CBC4"
                    )
                ),
                displayedItems = listOf(
                    Note(
                        id = "1",
                        text = stringResource(R.string.preview_note_text),
                        createdAt = System.currentTimeMillis(),
                        backgroundColorHex = "#80CBC4"
                    )
                )
            ),
            listState = rememberLazyListState(),
            onOpenAddDialog = {},
            onSearchQueryChange = {},
            onSortOrderChange = {},
            onDelete = {},
            onOpenEditDialog = {},
            onDismissDialog = {},
            onDialogInputChange = {},
            onConfirmDialog = {}
        )
    }
}