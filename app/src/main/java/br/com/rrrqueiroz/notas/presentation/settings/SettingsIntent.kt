package br.com.rrrqueiroz.notas.presentation.settings

sealed class SettingsIntent {
    object LoadNotesCount : SettingsIntent()
    data class ShowDeleteDialog(val show: Boolean) : SettingsIntent()
    object DeleteAllNotes : SettingsIntent()
}
