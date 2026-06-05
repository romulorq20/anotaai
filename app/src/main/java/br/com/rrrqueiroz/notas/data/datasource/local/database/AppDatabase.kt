package br.com.rrrqueiroz.notas.data.datasource.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.AudioNoteEntity
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.ChecklistNoteEntity
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.ImageNoteEntity
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.NoteEntity
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.TextNoteEntity


@Database(
    entities = [NoteEntity::class,
        TextNoteEntity::class,
        AudioNoteEntity::class,
        ImageNoteEntity::class,
        ChecklistNoteEntity::class
    ],
    version = 2,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun textNoteDao(): TextNoteDao
    abstract fun audioNoteDao(): AudioNoteDao
    abstract fun imageNoteDao(): ImageNoteDao
    abstract fun checklistNoteDao(): ChecklistNoteDao
}
