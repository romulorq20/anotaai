package br.com.rrrqueiroz.notas.data.repository

import br.com.rrrqueiroz.notas.data.datasource.local.NoteLocalDataSource
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.toNote
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.toNoteItemAudio
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.toNoteItemImage
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.toNoteItemText
import br.com.rrrqueiroz.notas.data.mapper.toEntity
import br.com.rrrqueiroz.notas.domain.model.BaseNote
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.model.NoteItemAudio
import br.com.rrrqueiroz.notas.domain.model.NoteItemImage
import br.com.rrrqueiroz.notas.domain.model.NoteItemText
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class NoteRepositoryImpl @Inject constructor(
    private val localDataSource: NoteLocalDataSource
) : NoteRepository {

    override suspend fun addNote(note: Note) {
        localDataSource.insertNote(note.toEntity())
        note.listItems.forEach { noteItem ->
            val currentTime = System.currentTimeMillis()
            when (noteItem) {
                is NoteItemText -> localDataSource.insertTextNote(
                    noteItem.toEntity().copy(idMainNote = note.id, date = currentTime)
                )

                is NoteItemImage -> localDataSource.insertImageNote(
                    noteItem.toEntity().copy(idMainNote = note.id, date = currentTime)
                )

                is NoteItemAudio -> localDataSource.insertAudioNote(
                    noteItem.toEntity().copy(idMainNote = note.id, date = currentTime)
                )
            }
        }
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return localDataSource.getAllNotes()
            .map { noteList -> noteList.map { it.toNote() }.sortedByDescending { it.date } }
    }

    override suspend fun getNoteById(noteId: String): Note? {
        val noteEntity = localDataSource.getNoteById(noteId) ?: return null
        val textNotes = localDataSource.getTextNotesByMainNoteId(noteEntity.id).map { it.toNoteItemText() }
        val imageNotes = localDataSource.getImageNotesByMainNoteId(noteEntity.id).map { it.toNoteItemImage() }
        val audioNotes = localDataSource.getAudioNotesByMainNoteId(noteEntity.id).map { it.toNoteItemAudio() }
        return Note(
            id = noteEntity.id,
            title = noteEntity.title,
            date = noteEntity.date,
            listItems = textNotes + imageNotes + audioNotes
        )
    }

    override suspend fun removeNote(note: Note) {
        localDataSource.deleteNote(note.toEntity())
        localDataSource.deleteTextNotesByMainNoteId(note.id)
        localDataSource.deleteImageNotesByMainNoteId(note.id)
        localDataSource.deleteAudioNotesByMainNoteId(note.id)
    }

    override suspend fun removeItemNote(noteItem: BaseNote) {
        when (noteItem) {
            is NoteItemText -> localDataSource.deleteTextNoteById(noteItem.id)
            is NoteItemImage -> localDataSource.deleteImageNoteById(noteItem.id)
            is NoteItemAudio -> localDataSource.deleteAudioNoteById(noteItem.id)
        }
    }

    override suspend fun deleteAllNotes() {
        localDataSource.deleteAllNotes()
        localDataSource.deleteAllTextNotes()
        localDataSource.deleteAllImageNotes()
        localDataSource.deleteAllAudioNotes()
    }

    override suspend fun countNotes(): Int {
        return localDataSource.countNotes()
    }

}
