package br.com.rrrqueiroz.notas.domain.usecase

import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import javax.inject.Inject

class SaveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.addNote(note)
    }
}
