package br.com.rrrqueiroz.notas.data.datasource.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.rrrqueiroz.notas.domain.model.NoteItemText
import java.util.UUID

@Entity(tableName = "TextNotes")
data class TextNoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val idMainNote: String = "",
    val date: Long = 0L,
    val content: String = ""
)

fun TextNoteEntity.toNoteItemText(): NoteItemText {
    return NoteItemText(
        id = id,
        idMainNote = idMainNote,
        date = date,
        content = content
    )
}
