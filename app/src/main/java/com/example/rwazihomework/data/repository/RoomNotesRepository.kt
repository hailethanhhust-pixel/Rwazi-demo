package com.example.rwazihomework.data.repository

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import com.example.rwazihomework.R
import com.example.rwazihomework.data.local.NoteEntity
import com.example.rwazihomework.data.local.NotesDatabase
import com.example.rwazihomework.domain.model.Note
import com.example.rwazihomework.domain.repository.NotesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.dataStore by preferencesDataStore(name = "home_items")
private val itemsKey = stringPreferencesKey("items_json")

@Singleton
class RoomNotesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: NotesDatabase
) : NotesRepository {

    override fun notesFlow(): Flow<List<Note>> {
        return database.notesDao().observeAll().map { entities ->
            entities.map { it.toNote() }
        }
    }

    override suspend fun addNote(item: Note) {
        database.notesDao().insert(item.toEntity())
    }

    override suspend fun updateNoteText(id: String, text: String) {
        database.notesDao().updateText(id = id, text = text)
    }

    override suspend fun deleteNoteById(id: String) {
        database.notesDao().deleteById(id)
    }

    override suspend fun ensureMinimumDefaultNotes(minimumCount: Int) {
        database.withTransaction {
            val dao = database.notesDao()
            if (dao.count() == 0) {
                val migratedItems = decodeLegacyDataStore()
                if (migratedItems.isNotEmpty()) {
                    dao.upsertAll(migratedItems.map { it.toEntity() })
                    context.dataStore.edit { preferences ->
                        preferences.remove(itemsKey)
                    }
                }
            }

            val existingItems = dao.getAll().map { it.toNote() }
            if (existingItems.size < minimumCount) {
                val notesToAdd = minimumCount - existingItems.size
                val usedColors = existingItems.map { it.backgroundColorHex }.toSet()
                val mergedItems = existingItems + defaultTestNotes(count = notesToAdd, usedColors = usedColors)
                dao.deleteAll()
                dao.upsertAll(mergedItems.map { it.toEntity() })
            }
        }
    }

    private suspend fun decodeLegacyDataStore(): List<Note> {
        val raw = context.dataStore.data.first()[itemsKey]
        return LegacyNotesParser.parse(raw) { usedColors ->
            randomVibrantColorHex(usedColors)
        }
    }

    private fun defaultTestNotes(count: Int, usedColors: Set<String> = emptySet()): List<Note> {
        val now = System.currentTimeMillis()
        val colors = usedColors.toMutableSet()
        return List(count) { index ->
            val color = randomVibrantColorHex(colors)
            colors.add(color)
            Note(
                id = UUID.randomUUID().toString(),
                text = context.getString(R.string.sample_note_text, index + 1),
                createdAt = now - (index * 60_000L),
                backgroundColorHex = color
            )
        }
    }

    private fun randomVibrantColorHex(usedColors: Set<String>): String {
        repeat(24) {
            val hue = Random.nextInt(0, 360).toFloat()
            val saturation = Random.nextDouble(0.65, 0.95).toFloat()
            val value = Random.nextDouble(0.75, 0.98).toFloat()
            val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
            val hex = String.format(Locale.US, "#%06X", 0xFFFFFF and colorInt)
            if (!usedColors.contains(hex)) {
                return hex
            }
        }

        val hue = Random.nextInt(0, 360).toFloat()
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, 0.75f, 0.85f))
        return String.format(Locale.US, "#%06X", 0xFFFFFF and colorInt)
    }
}

private fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        text = text,
        createdAt = createdAt,
        backgroundColorHex = backgroundColorHex
    )
}

private fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        text = text,
        createdAt = createdAt,
        backgroundColorHex = backgroundColorHex
    )
}
