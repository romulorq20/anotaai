package br.com.rrrqueiroz.notas.presentation.home

import br.com.rrrqueiroz.notas.domain.model.Note

sealed class HomeIntent {
    object LoadNotes : HomeIntent()
    data class DeleteNote(val note: Note) : HomeIntent()
    data class SetItemToDelete(val note: Note?) : HomeIntent()
    data class ToggleAudio(val noteId: String, val audioPath: String) : HomeIntent()
    object StopAudio : HomeIntent()
}
