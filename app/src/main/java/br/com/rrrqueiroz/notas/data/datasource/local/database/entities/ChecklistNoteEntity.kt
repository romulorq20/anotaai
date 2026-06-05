package br.com.rrrqueiroz.notas.data.datasource.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.rrrqueiroz.notas.domain.model.NoteItemChecklist
import java.util.UUID

@Entity(tableName = "ChecklistNotes")
data class ChecklistNoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val idMainNote: String = "",
    val date: Long = 0L,
    val content: String = "",
    val isChecked: Boolean = false
)

fun ChecklistNoteEntity.toNoteItemChecklist(): NoteItemChecklist {
    return NoteItemChecklist(
        id = id,
        idMainNote = idMainNote,
        date = date,
        content = content,
        isChecked = isChecked
    )
}
