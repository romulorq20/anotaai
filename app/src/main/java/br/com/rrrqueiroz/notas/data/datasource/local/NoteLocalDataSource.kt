package br.com.rrrqueiroz.notas.data.datasource.local

import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.*
import kotlinx.coroutines.flow.Flow

interface NoteLocalDataSource {
    suspend fun insertNote(note: NoteEntity)
    suspend fun insertTextNote(textNote: TextNoteEntity)
    suspend fun insertImageNote(imageNote: ImageNoteEntity)
    suspend fun insertAudioNote(audioNote: AudioNoteEntity)
    suspend fun insertChecklistNote(checklistNote: ChecklistNoteEntity)
    
    fun getAllNotes(): Flow<List<NoteEntity>>
    suspend fun getNoteById(id: String): NoteEntity?
    
    suspend fun getTextNotesByMainNoteId(idMainNote: String): List<TextNoteEntity>
    suspend fun getImageNotesByMainNoteId(idMainNote: String): List<ImageNoteEntity>
    suspend fun getAudioNotesByMainNoteId(idMainNote: String): List<AudioNoteEntity>
    suspend fun getChecklistNotesByMainNoteId(idMainNote: String): List<ChecklistNoteEntity>
    
    suspend fun deleteNote(note: NoteEntity)
    suspend fun deleteTextNotesByMainNoteId(idMainNote: String)
    suspend fun deleteImageNotesByMainNoteId(idMainNote: String)
    suspend fun deleteAudioNotesByMainNoteId(idMainNote: String)
    suspend fun deleteChecklistNotesByMainNoteId(idMainNote: String)
    
    suspend fun deleteTextNoteById(id: String)
    suspend fun deleteImageNoteById(id: String)
    suspend fun deleteAudioNoteById(id: String)
    suspend fun deleteChecklistNoteById(id: String)
    
    suspend fun deleteAllNotes()
    suspend fun deleteAllTextNotes()
    suspend fun deleteAllImageNotes()
    suspend fun deleteAllAudioNotes()
    suspend fun deleteAllChecklistNotes()

    suspend fun countNotes(): Int
}
