package br.com.rrrqueiroz.notas.presentation.notes

import br.com.rrrqueiroz.notas.domain.model.BaseNote
import br.com.rrrqueiroz.notas.domain.model.NoteType

sealed class NoteIntent {
    data class LoadNote(val noteId: String) : NoteIntent()
    object SaveNote : NoteIntent()
    data class DeleteItem(val noteItem: BaseNote) : NoteIntent()
    data class UpdateTitle(val title: String) : NoteIntent()
    data class AddImage(val imageLink: String) : NoteIntent()
    object AddAudio : NoteIntent()
    object AddText : NoteIntent()
    data class UpdateItemText(val id: String, val newText: String) : NoteIntent()
    data class UpdateNoteText(val text: String) : NoteIntent()
    data class ToggleCamera(val show: Boolean) : NoteIntent()
    data class ToggleRecording(val recording: Boolean) : NoteIntent()
    data class ToggleAudioNote(val add: Boolean) : NoteIntent()
    data class UpdateAudioDuration(val duration: Int) : NoteIntent()
    data class SetAudioPath(val path: String) : NoteIntent()
    object ResetNote : NoteIntent()
    data class InitializeNoteType(val type: NoteType) : NoteIntent()
    object AddChecklistItem : NoteIntent()
    data class UpdateChecklistItem(val id: String, val text: String, val isChecked: Boolean) : NoteIntent()
    object StartRecording : NoteIntent()
    object StopRecording : NoteIntent()
    data class PlayAudio(val path: String) : NoteIntent()
    object StopAudio : NoteIntent()
}
