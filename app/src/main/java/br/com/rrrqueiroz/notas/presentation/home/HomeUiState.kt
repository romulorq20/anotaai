package br.com.rrrqueiroz.notas.presentation.home

import br.com.rrrqueiroz.notas.domain.model.Note

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val itemToDelete: Note? = null,
    val playingNoteId: String? = null
)
