package br.com.rrrqueiroz.notas.domain.usecase

import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke() = repository.deleteAllNotes()
}
