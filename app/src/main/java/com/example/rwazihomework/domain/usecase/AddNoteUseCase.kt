package com.example.rwazihomework.domain.usecase

import com.example.rwazihomework.domain.model.Note
import com.example.rwazihomework.domain.repository.NotesRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(item: Note) {
        repository.addNote(item)
    }
}
