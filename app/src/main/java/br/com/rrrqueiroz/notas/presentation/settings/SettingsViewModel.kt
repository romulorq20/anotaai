package br.com.rrrqueiroz.notas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        handleIntent(SettingsIntent.LoadNotesCount)
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadNotesCount -> countNotes()
            is SettingsIntent.ShowDeleteDialog -> showDeleteDialog(intent.show)
            is SettingsIntent.DeleteAllNotes -> deleteAllNotes()
        }
    }

    private fun countNotes() {
        viewModelScope.launch {
            val count = noteRepository.countNotes()
            _uiState.update { it.copy(notesCount = count) }
        }
    }

    private fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showConfirmDeleteDialog = show) }
    }

    private fun deleteAllNotes() {
        viewModelScope.launch {
            noteRepository.deleteAllNotes()
            _uiState.update { it.copy(notesCount = 0, showConfirmDeleteDialog = false) }
            _effect.emit(SettingsEffect.ShowMessage("Todas as notas foram apagadas"))
        }
    }
}
