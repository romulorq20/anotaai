package br.com.rrrqueiroz.notas.domain.repository

import br.com.rrrqueiroz.notas.domain.model.BaseNote
import br.com.rrrqueiroz.notas.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun addNote(note: Note)
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(noteId: String): Note?
    suspend fun removeNote(note: Note)
    suspend fun removeItemNote(noteItem: BaseNote)
    suspend fun deleteAllNotes()
    suspend fun countNotes(): Int
}
