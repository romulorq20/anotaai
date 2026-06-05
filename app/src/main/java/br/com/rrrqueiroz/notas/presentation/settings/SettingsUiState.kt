package br.com.rrrqueiroz.notas.presentation.settings

data class SettingsUiState(
    val notesCount: Int = 0,
    val showConfirmDeleteDialog: Boolean = false
)
