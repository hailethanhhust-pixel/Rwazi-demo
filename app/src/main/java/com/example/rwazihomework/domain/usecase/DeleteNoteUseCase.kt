package com.example.rwazihomework.domain.usecase

import com.example.rwazihomework.domain.repository.NotesRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteNoteById(id)
    }
}
