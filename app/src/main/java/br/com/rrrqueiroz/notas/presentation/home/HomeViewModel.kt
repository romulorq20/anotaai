package br.com.rrrqueiroz.notas.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import br.com.rrrqueiroz.notas.utils.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect = _effect.asSharedFlow()

    init {
        handleIntent(HomeIntent.LoadNotes)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadNotes -> loadNotes()
            is HomeIntent.DeleteNote -> deleteNote(intent.note)
            is HomeIntent.SetItemToDelete -> setItemToDelete(intent.note)
            is HomeIntent.ToggleAudio -> toggleAudio(intent.noteId, intent.audioPath)
            is HomeIntent.StopAudio -> stopAudio()
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.removeNote(note)
        }
    }

    private fun setItemToDelete(note: Note?) {
        _uiState.update { it.copy(itemToDelete = note) }
    }

    private fun toggleAudio(noteId: String, audioPath: String) {
        val isCurrentlyPlaying = _uiState.value.playingNoteId == noteId
        if (isCurrentlyPlaying) {
            audioManager.stopPlaying()
            _uiState.update { it.copy(playingNoteId = null) }
        } else {
            try {
                audioManager.startPlaying(audioPath)
                _uiState.update { it.copy(playingNoteId = noteId) }
            } catch (e: IOException) {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.ShowMessage("Erro ao reproduzir áudio"))
                }
            }
        }
    }

    private fun stopAudio() {
        audioManager.stopPlaying()
        _uiState.update { it.copy(playingNoteId = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.stopPlaying()
    }
}
