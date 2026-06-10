package br.com.rrrqueiroz.notas.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.rrrqueiroz.notas.domain.model.BaseNote
import br.com.rrrqueiroz.notas.domain.model.NoteItemAudio
import br.com.rrrqueiroz.notas.domain.model.NoteItemChecklist
import br.com.rrrqueiroz.notas.domain.model.NoteItemImage
import br.com.rrrqueiroz.notas.domain.model.NoteItemText
import br.com.rrrqueiroz.notas.domain.model.NoteType
import br.com.rrrqueiroz.notas.domain.usecase.DeleteItemNoteUseCase
import br.com.rrrqueiroz.notas.domain.usecase.GetNoteUseCase
import br.com.rrrqueiroz.notas.domain.usecase.SaveNoteUseCase
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
class NoteViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val removeItemNoteUseCase: DeleteItemNoteUseCase,
    private val audioManager: AudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<NoteEffect>()
    val effect = _effect.asSharedFlow()

    fun handleIntent(intent: NoteIntent) {
        when (intent) {
            is NoteIntent.LoadNote -> loadNote(intent.noteId)
            NoteIntent.SaveNote -> saveNote()
            is NoteIntent.DeleteItem -> deleteItem(intent.noteItem)
            is NoteIntent.UpdateTitle -> _uiState.update { it.copy(noteTextAppBar = intent.title) }
            is NoteIntent.AddImage -> addNewItemImage(intent.imageLink)
            NoteIntent.AddAudio -> addNewItemAudio()
            NoteIntent.AddText -> addNewItemText()
            is NoteIntent.UpdateItemText -> updateItemText(intent.id, intent.newText)
            is NoteIntent.UpdateNoteText -> _uiState.update { it.copy(noteText = intent.text) }
            is NoteIntent.ToggleCamera -> _uiState.update { it.copy(showCameraScreen = intent.show) }
            is NoteIntent.ToggleRecording -> _uiState.update { it.copy(isRecording = intent.recording) }
            is NoteIntent.ToggleAudioNote -> _uiState.update { it.copy(addAudioNote = intent.add) }
            is NoteIntent.UpdateAudioDuration -> _uiState.update { it.copy(audioDuration = intent.duration) }
            is NoteIntent.SetAudioPath -> _uiState.update { it.copy(audioPath = intent.path) }
            NoteIntent.ResetNote -> _uiState.value = NoteUiState()
            is NoteIntent.InitializeNoteType -> initializeNoteType(intent.type)
            NoteIntent.AddChecklistItem -> addChecklistItem()
            is NoteIntent.UpdateChecklistItem -> updateChecklistItem(intent.id, intent.text, intent.isChecked)
            NoteIntent.StartRecording -> startRecording()
            NoteIntent.StopRecording -> stopRecording()
            is NoteIntent.PlayAudio -> playAudio(intent.path)
            NoteIntent.StopAudio -> audioManager.stopPlaying()
        }
    }

    private fun startRecording() {
        viewModelScope.launch {
            try {
                val path = audioManager.startRecording()
                _uiState.update { it.copy(audioPath = path, isRecording = true) }
            } catch (e: IOException) {
                _effect.emit(NoteEffect.ShowError("Erro ao iniciar gravação"))
            }
        }
    }

    private fun stopRecording() {
        audioManager.stopRecording()
        _uiState.update { it.copy(isRecording = false) }
        handleIntent(NoteIntent.AddAudio)
    }

    private fun playAudio(path: String) {
        viewModelScope.launch {
            try {
                audioManager.startPlaying(path)
            } catch (e: IOException) {
                _effect.emit(NoteEffect.ShowError("Erro ao reproduzir áudio"))
            }
        }
    }

    private fun initializeNoteType(type: NoteType) {
        _uiState.update { it.copy(note = it.note.copy(type = type)) }
        if (type == NoteType.LIST) {
            addChecklistItem()
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            val note = getNoteUseCase(noteId)
            note?.let {
                _uiState.update { currentState ->
                    currentState.copy(note = it, noteTextAppBar = it.title)
                }
            }
        }
    }

    private fun saveNote() {
        viewModelScope.launch {
            saveNoteUseCase(_uiState.value.note.copy(title = _uiState.value.noteTextAppBar))
            _effect.emit(NoteEffect.NavigateBack)
        }
    }

    private fun deleteItem(noteItem: BaseNote) {
        viewModelScope.launch {
            removeItemNoteUseCase(noteItem)
            loadNote(_uiState.value.note.id)
        }
    }

    private fun addNewItemImage(imageLink: String) {
        _uiState.update { currentState ->
            val listItems = currentState.note.listItems.toMutableList()
            listItems.add(NoteItemImage(link = imageLink, date = System.currentTimeMillis()))
            currentState.copy(note = currentState.note.copy(listItems = listItems, type = NoteType.IMAGE))
        }
    }

    private fun addNewItemAudio() {
        _uiState.update { currentState ->
            val listItems = currentState.note.listItems.toMutableList()
            listItems.add(
                NoteItemAudio(
                    link = currentState.audioPath,
                    duration = currentState.audioDuration,
                    date = System.currentTimeMillis()
                )
            )
            currentState.copy(
                note = currentState.note.copy(listItems = listItems, type = NoteType.AUDIO),
                addAudioNote = false
            )
        }
    }

    private fun addNewItemText() {
        _uiState.update { currentState ->
            val listItems = currentState.note.listItems.toMutableList()
            listItems.add(
                NoteItemText(
                    content = currentState.noteText,
                    date = System.currentTimeMillis()
                )
            )
            currentState.copy(
                note = currentState.note.copy(listItems = listItems, type = NoteType.TEXT),
                noteText = ""
            )
        }
    }

    private fun updateItemText(id: String, newText: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.note.listItems.map { item ->
                if (item.id == id && item is NoteItemText) item.copy(content = newText) else item
            }
            currentState.copy(note = currentState.note.copy(listItems = updatedList))
        }
    }

    private fun addChecklistItem() {
        _uiState.update { currentState ->
            val listItems = currentState.note.listItems.toMutableList()
            listItems.add(NoteItemChecklist(
                idMainNote = currentState.note.id,
                date = System.currentTimeMillis()
            ))
            currentState.copy(note = currentState.note.copy(listItems = listItems, type = NoteType.LIST))
        }
    }

    private fun updateChecklistItem(id: String, text: String, isChecked: Boolean) {
        _uiState.update { currentState ->
            val updatedList = currentState.note.listItems.map { item ->
                if (item.id == id && item is NoteItemChecklist) {
                    item.copy(content = text, isChecked = isChecked)
                } else item
            }
            currentState.copy(note = currentState.note.copy(listItems = updatedList))
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}
