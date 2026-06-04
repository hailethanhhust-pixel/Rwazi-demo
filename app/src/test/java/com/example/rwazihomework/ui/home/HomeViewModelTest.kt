package com.example.rwazihomework.ui.home

import com.example.rwazihomework.MainDispatcherRule
import com.example.rwazihomework.domain.model.Note
import com.example.rwazihomework.domain.repository.NotesRepository
import com.example.rwazihomework.domain.usecase.AddNoteUseCase
import com.example.rwazihomework.domain.usecase.DeleteNoteUseCase
import com.example.rwazihomework.domain.usecase.EnsureMinimumDefaultNotesUseCase
import com.example.rwazihomework.domain.usecase.ObserveNotesUseCase
import com.example.rwazihomework.domain.usecase.UpdateNoteTextUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onConfirmDialog_addsNote_whenNotEditing() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = buildViewModel(repository)

        viewModel.onOpenAddDialog()
        viewModel.onDialogInputChange("My note")
        viewModel.onConfirmDialog()

        advanceUntilIdle()

        assertEquals(1, repository.addedNotes.size)
        assertEquals("My note", repository.addedNotes.first().text)
        assertFalse(viewModel.uiState.value.showDialog)
    }

    @Test
    fun onConfirmDialog_updatesNote_whenEditing() = runTest {
        val repository = FakeNotesRepository()
        val existing = Note(
            id = "id-1",
            text = "Old",
            createdAt = 100L,
            backgroundColorHex = "#123456"
        )
        repository.emit(listOf(existing))
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onOpenEditDialog(existing)
        viewModel.onDialogInputChange("Updated")
        viewModel.onConfirmDialog()

        advanceUntilIdle()

        assertEquals(1, repository.updatedNotes.size)
        assertEquals(Pair("id-1", "Updated"), repository.updatedNotes.first())
    }

    @Test
    fun onDelete_deletesById() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = buildViewModel(repository)

        viewModel.onDelete("note-42")
        advanceUntilIdle()

        assertEquals(listOf("note-42"), repository.deletedNoteIds)
    }

    private fun buildViewModel(repository: FakeNotesRepository): HomeViewModel {
        return HomeViewModel(
            observeNotesUseCase = ObserveNotesUseCase(repository),
            addNoteUseCase = AddNoteUseCase(repository),
            updateNoteTextUseCase = UpdateNoteTextUseCase(repository),
            deleteNoteUseCase = DeleteNoteUseCase(repository),
            ensureMinimumDefaultNotesUseCase = EnsureMinimumDefaultNotesUseCase(repository)
        )
    }

    private class FakeNotesRepository : NotesRepository {
        private val notes = MutableStateFlow<List<Note>>(emptyList())

        val addedNotes = mutableListOf<Note>()
        val updatedNotes = mutableListOf<Pair<String, String>>()
        val deletedNoteIds = mutableListOf<String>()

        override fun notesFlow(): Flow<List<Note>> = notes

        override suspend fun addNote(item: Note) {
            addedNotes.add(item)
            notes.value = notes.value + item
        }

        override suspend fun updateNoteText(id: String, text: String) {
            updatedNotes.add(id to text)
            notes.value = notes.value.map { if (it.id == id) it.copy(text = text) else it }
        }

        override suspend fun deleteNoteById(id: String) {
            deletedNoteIds.add(id)
            notes.value = notes.value.filterNot { it.id == id }
        }

        override suspend fun ensureMinimumDefaultNotes(minimumCount: Int) {
        }

        fun emit(items: List<Note>) {
            notes.value = items
        }
    }
}
