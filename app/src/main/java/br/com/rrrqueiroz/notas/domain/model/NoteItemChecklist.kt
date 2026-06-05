package br.com.rrrqueiroz.notas.domain.model

import java.util.UUID

data class NoteItemChecklist(
    override val id: String = UUID.randomUUID().toString(),
    override val idMainNote: String = "",
    override val date: Long = System.currentTimeMillis(),
    val content: String = "",
    val isChecked: Boolean = false
) : BaseNote(
    id = id,
    idMainNote = idMainNote,
    date = date,
    type = NoteType.LIST
)
