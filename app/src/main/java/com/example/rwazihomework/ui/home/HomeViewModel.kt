package com.example.rwazihomework.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rwazihomework.domain.model.Note
import com.example.rwazihomework.domain.model.nextNoteColorHex
import com.example.rwazihomework.domain.usecase.AddNoteUseCase
import com.example.rwazihomework.domain.usecase.DeleteNoteUseCase
import com.example.rwazihomework.domain.usecase.EnsureMinimumDefaultNotesUseCase
import com.example.rwazihomework.domain.usecase.ObserveNotesUseCase
import com.example.rwazihomework.domain.usecase.UpdateNoteTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val PAGE_SIZE = 30
private const val LOAD_MORE_THRESHOLD = 6

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeNotesUseCase: ObserveNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteTextUseCase: UpdateNoteTextUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val ensureMinimumDefaultNotesUseCase: EnsureMinimumDefaultNotesUseCase
) : ViewModel() {

    private val allItems = MutableStateFlow<List<Note>>(emptyList())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ensureMinimumDefaultNotesUseCase()
            observeNotesUseCase().collect { notes ->
                allItems.value = notes
                recompute(resetPagination = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        recompute(resetPagination = true)
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
        recompute(resetPagination = true)
    }

    fun onOpenAddDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingNoteId = null,
                inputText = ""
            )
        }
    }

    fun onOpenEditDialog(note: Note) {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingNoteId = note.id,
                inputText = note.text
            )
        }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showDialog = false,
                inputText = "",
                editingNoteId = null
            )
        }
    }

    fun onDialogInputChange(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun onConfirmDialog() {
        val currentState = _uiState.value
        val valueToAdd = currentState.inputText.trim()
        if (valueToAdd.isEmpty()) {
            onDismissDialog()
            return
        }

        val currentItems = allItems.value
        if (currentState.editingNoteId == null) {
            val newNote = Note(
                id = UUID.randomUUID().toString(),
                text = valueToAdd,
                createdAt = System.currentTimeMillis(),
                backgroundColorHex = nextNoteColorHex(
                    currentItems.map { it.backgroundColorHex }.toSet()
                )
            )
            viewModelScope.launch {
                addNoteUseCase(newNote)
            }
        } else {
            viewModelScope.launch {
                updateNoteTextUseCase(
                    id = currentState.editingNoteId,
                    text = valueToAdd
                )
            }
        }
        onDismissDialog()
    }

    fun onDelete(noteId: String) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId)
        }
    }

    fun onListScrolled(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (!state.hasMoreItems || state.isLoadingMore) return

        val triggerIndex = maxOf(0, state.displayedItems.lastIndex - LOAD_MORE_THRESHOLD)
        if (lastVisibleIndex < triggerIndex || triggerIndex == state.lastPaginationTriggerIndex) return

        _uiState.update {
            val nextVisibleCount = minOf(it.visibleCount + PAGE_SIZE, it.filteredItems.size)
            it.copy(
                visibleCount = nextVisibleCount,
                displayedItems = it.filteredItems.take(nextVisibleCount),
                isLoadingMore = false,
                hasMoreItems = nextVisibleCount < it.filteredItems.size,
                lastPaginationTriggerIndex = triggerIndex
            )
        }
    }

    private fun recompute(resetPagination: Boolean) {
        val state = _uiState.value
        val sortedItems = when (state.sortOrder) {
            SortOrder.Newest -> allItems.value.sortedByDescending { it.createdAt }
            SortOrder.Oldest -> allItems.value.sortedBy { it.createdAt }
        }

        val query = state.searchQuery.trim()
        val filteredItems = if (query.isEmpty()) {
            sortedItems
        } else {
            sortedItems.filter { note -> note.text.contains(query, ignoreCase = true) }
        }

        val visibleCount = if (resetPagination) {
            minOf(PAGE_SIZE, filteredItems.size)
        } else {
            minOf(state.visibleCount, filteredItems.size)
        }

        _uiState.update {
            it.copy(
                filteredItems = filteredItems,
                displayedItems = filteredItems.take(visibleCount),
                visibleCount = visibleCount,
                hasMoreItems = visibleCount < filteredItems.size,
                isLoadingMore = false,
                lastPaginationTriggerIndex = -1
            )
        }
    }
}

data class HomeUiState(
    val filteredItems: List<Note> = emptyList(),
    val displayedItems: List<Note> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.Newest,
    val showDialog: Boolean = false,
    val inputText: String = "",
    val editingNoteId: String? = null,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = false,
    val visibleCount: Int = PAGE_SIZE,
    val lastPaginationTriggerIndex: Int = -1
)
