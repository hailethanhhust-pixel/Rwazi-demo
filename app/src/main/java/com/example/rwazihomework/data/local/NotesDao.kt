package com.example.rwazihomework.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NoteEntity)

    @Query("UPDATE notes SET text = :text WHERE id = :id")
    suspend fun updateText(id: String, text: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<NoteEntity>)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
