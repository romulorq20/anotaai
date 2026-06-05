package br.com.rrrqueiroz.notas.domain.usecase

import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import br.com.rrrqueiroz.notas.domain.model.Note
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.removeNote(note)
    }
}
