package br.com.rrrqueiroz.notas.domain.model

import java.util.UUID

data class NoteItemAudio(
    override val id: String = UUID.randomUUID().toString(),
    override val idMainNote: String = "",
    override val date: Long = 0L,
    val link: String,
    val duration: Int,
) : BaseNote(
    id = id,
    idMainNote = idMainNote,
    date = date,
    type = NoteType.AUDIO
)
