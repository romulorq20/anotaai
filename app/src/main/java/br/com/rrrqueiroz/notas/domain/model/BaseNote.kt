package br.com.rrrqueiroz.notas.domain.model

abstract class BaseNote(
    open val id: String,
    open val idMainNote: String,
    open val date: Long,
    val type: NoteType
)

enum class NoteType {
    TEXT, IMAGE, AUDIO, LIST
}
