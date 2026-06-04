package com.example.rwazihomework.di

import android.content.Context
import androidx.room.Room
import com.example.rwazihomework.data.local.NotesDao
import com.example.rwazihomework.data.local.NotesDatabase
import com.example.rwazihomework.data.repository.RoomNotesRepository
import com.example.rwazihomework.domain.repository.NotesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context): NotesDatabase {
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            "notes.db"
        ).build()
    }

    @Provides
    fun provideNotesDao(database: NotesDatabase): NotesDao {
        return database.notesDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotesRepository(repository: RoomNotesRepository): NotesRepository
}
