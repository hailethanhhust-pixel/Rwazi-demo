package com.example.rwazihomework.domain.usecase

import com.example.rwazihomework.domain.repository.NotesRepository
import javax.inject.Inject

class EnsureMinimumDefaultNotesUseCase @Inject constructor(
    private val repository: NotesRepository
) {
    suspend operator fun invoke(minimumCount: Int = 50) {
        repository.ensureMinimumDefaultNotes(minimumCount)
    }
}
