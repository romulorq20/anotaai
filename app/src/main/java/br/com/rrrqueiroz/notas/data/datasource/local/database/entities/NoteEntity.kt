package br.com.rrrqueiroz.notas.data.datasource.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.model.NoteType
import java.util.UUID

@Entity(tableName = "Notes")
data class NoteEntity(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: Long = System.currentTimeMillis(),
    val thumbnail: String? = null,
    val type: String = NoteType.TEXT.name
)

fun NoteEntity.toNote() = Note(
    id = id,
    title = title,
    date = date,
    thumbnail = thumbnail,
    type = NoteType.valueOf(type)
)
