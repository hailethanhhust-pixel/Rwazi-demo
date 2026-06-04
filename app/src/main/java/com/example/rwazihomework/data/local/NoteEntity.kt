package com.example.rwazihomework.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val createdAt: Long,
    val backgroundColorHex: String
)
