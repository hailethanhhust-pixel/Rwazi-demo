package com.example.rwazihomework.domain.repository

import com.example.rwazihomework.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun notesFlow(): Flow<List<Note>>
    suspend fun addNote(item: Note)
    suspend fun updateNoteText(id: String, text: String)
    suspend fun deleteNoteById(id: String)
    suspend fun ensureMinimumDefaultNotes(minimumCount: Int = 50)
}
