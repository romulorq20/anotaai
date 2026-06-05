package br.com.rrrqueiroz.notas.domain.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: Long = System.currentTimeMillis(),
    val listItems: List<BaseNote> = emptyList(),
    val thumbnail: String? = null,
    val type: NoteType = NoteType.TEXT
)
