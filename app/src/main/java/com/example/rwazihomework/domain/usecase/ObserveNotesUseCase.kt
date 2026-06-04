package com.example.rwazihomework.domain.usecase

import com.example.rwazihomework.domain.model.Note
import com.example.rwazihomework.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotesUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.notesFlow()
}
