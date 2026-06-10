package br.com.rrrqueiroz.notas.domain.usecase

import br.com.rrrqueiroz.notas.domain.model.BaseNote
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteItemNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: BaseNote) {
        repository.removeItemNote(note)
    }
}
