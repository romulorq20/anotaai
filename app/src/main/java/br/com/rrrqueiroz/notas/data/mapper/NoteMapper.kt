package br.com.rrrqueiroz.notas.data.mapper

import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.*
import br.com.rrrqueiroz.notas.domain.model.*

fun Note.toEntity(): NoteEntity {
    var thumbnail: String? = null
    var type: NoteType = NoteType.TEXT
    
    if (this.listItems.isNotEmpty()) {
        val lastItem = this.listItems.last()
        type = lastItem.type
        thumbnail = when (lastItem) {
            is NoteItemText -> NoteType.TEXT.name
            is NoteItemImage -> lastItem.link
            is NoteItemAudio -> lastItem.link
            is NoteItemChecklist -> NoteType.LIST.name
            else -> NoteType.TEXT.name
        }
    }

    return NoteEntity(
        id = id,
        title = title,
        date = date,
        thumbnail = thumbnail,
        type = type.name
    )
}

fun NoteItemText.toEntity(): TextNoteEntity = TextNoteEntity(
    id = id,
    idMainNote = idMainNote,
    date = date,
    content = content
)

fun NoteItemImage.toEntity(): ImageNoteEntity = ImageNoteEntity(
    id = id,
    idMainNote = idMainNote,
    date = date,
    link = link
)

fun NoteItemAudio.toEntity(): AudioNoteEntity = AudioNoteEntity(
    id = id,
    idMainNote = idMainNote,
    date = date,
    link = link,
    duration = duration
)

fun NoteItemChecklist.toEntity(): ChecklistNoteEntity = ChecklistNoteEntity(
    id = id,
    idMainNote = idMainNote,
    date = date,
    content = content,
    isChecked = isChecked
)
