package br.com.rrrqueiroz.notas.data.datasource.local

import br.com.rrrqueiroz.notas.data.datasource.local.database.*
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomNoteDataSource @Inject constructor(
    private val noteDao: NoteDao,
    private val textNoteDao: TextNoteDao,
    private val imageNoteDao: ImageNoteDao,
    private val audioNoteDao: AudioNoteDao,
    private val checklistNoteDao: ChecklistNoteDao,
) : NoteLocalDataSource {
    override suspend fun insertNote(note: NoteEntity) {
        noteDao.insert(note)
    }

    override suspend fun insertTextNote(textNote: TextNoteEntity) {
        textNoteDao.insert(textNote)
    }

    override suspend fun insertImageNote(imageNote: ImageNoteEntity) {
        imageNoteDao.insert(imageNote)
    }

    override suspend fun insertAudioNote(audioNote: AudioNoteEntity) {
        audioNoteDao.insert(audioNote)
    }

    override suspend fun insertChecklistNote(checklistNote: ChecklistNoteEntity) {
        checklistNoteDao.insert(checklistNote)
    }

    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    override suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    override suspend fun getTextNotesByMainNoteId(idMainNote: String): List<TextNoteEntity> =
        textNoteDao.getByIdMainNote(idMainNote)

    override suspend fun getImageNotesByMainNoteId(idMainNote: String): List<ImageNoteEntity> =
        imageNoteDao.getByIdMainNote(idMainNote)

    override suspend fun getAudioNotesByMainNoteId(idMainNote: String): List<AudioNoteEntity> =
        audioNoteDao.getByIdMainNote(idMainNote)

    override suspend fun getChecklistNotesByMainNoteId(idMainNote: String): List<ChecklistNoteEntity> =
        checklistNoteDao.getByIdMainNote(idMainNote)

    override suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
    }

    override suspend fun deleteTextNotesByMainNoteId(idMainNote: String) {
        textNoteDao.deleteByIdMainNote(idMainNote)
    }

    override suspend fun deleteImageNotesByMainNoteId(idMainNote: String) {
        imageNoteDao.deleteByIdMainNote(idMainNote)
    }

    override suspend fun deleteAudioNotesByMainNoteId(idMainNote: String) {
        audioNoteDao.deleteByIdMainNote(idMainNote)
    }

    override suspend fun deleteChecklistNotesByMainNoteId(idMainNote: String) {
        checklistNoteDao.deleteByIdMainNote(idMainNote)
    }

    override suspend fun deleteTextNoteById(id: String) {
        textNoteDao.delete(id)
    }

    override suspend fun deleteImageNoteById(id: String) {
        imageNoteDao.delete(id)
    }

    override suspend fun deleteAudioNoteById(id: String) {
        audioNoteDao.delete(id)
    }

    override suspend fun deleteChecklistNoteById(id: String) {
        checklistNoteDao.delete(id)
    }

    override suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    override suspend fun deleteAllTextNotes() {
        textNoteDao.deleteAll()
    }

    override suspend fun deleteAllImageNotes() {
        imageNoteDao.deleteAll()
    }

    override suspend fun deleteAllAudioNotes() {
        audioNoteDao.deleteAllAudioNotes()
    }

    override suspend fun deleteAllChecklistNotes() {
        checklistNoteDao.deleteAll()
    }

    override suspend fun countNotes(): Int = noteDao.countNotes()
}
