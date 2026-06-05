package br.com.rrrqueiroz.notas.presentation.notes

sealed class NoteEffect {
    object NavigateBack : NoteEffect()
    data class ShowError(val message: String) : NoteEffect()
}
