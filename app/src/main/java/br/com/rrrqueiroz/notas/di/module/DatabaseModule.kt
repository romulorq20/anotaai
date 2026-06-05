package br.com.rrrqueiroz.notas.di.module

import android.content.Context
import androidx.room.Room
import br.com.rrrqueiroz.notas.data.datasource.local.database.AppDatabase
import br.com.rrrqueiroz.notas.data.datasource.local.database.AudioNoteDao
import br.com.rrrqueiroz.notas.data.datasource.local.database.ChecklistNoteDao
import br.com.rrrqueiroz.notas.data.datasource.local.database.ImageNoteDao
import br.com.rrrqueiroz.notas.data.datasource.local.database.NoteDao
import br.com.rrrqueiroz.notas.data.datasource.local.database.TextNoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "anotaai.db"

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao {
        return db.noteDao()
    }

    @Provides
    fun provideTextNoteDao(db: AppDatabase): TextNoteDao {
        return db.textNoteDao()
    }

    @Provides
    fun provideAudioNoteDao(db: AppDatabase): AudioNoteDao {
        return db.audioNoteDao()
    }

    @Provides
    fun provideImageNoteDao(db: AppDatabase): ImageNoteDao {
        return db.imageNoteDao()
    }

    @Provides
    fun provideChecklistNoteDao(db: AppDatabase): ChecklistNoteDao {
        return db.checklistNoteDao()
    }
}
