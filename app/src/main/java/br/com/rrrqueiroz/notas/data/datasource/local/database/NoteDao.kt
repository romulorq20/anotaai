package br.com.rrrqueiroz.notas.data.datasource.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.NoteEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Query("SELECT * FROM Notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM Notes")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM Notes")
    suspend fun deleteAllNotes()

    @Query("SELECT COUNT(*) FROM Notes")
    suspend fun countNotes(): Int
}
