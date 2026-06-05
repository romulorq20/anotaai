package br.com.rrrqueiroz.notas.data.datasource.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.rrrqueiroz.notas.data.datasource.local.database.entities.ChecklistNoteEntity

@Dao
interface ChecklistNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: ChecklistNoteEntity): Long

    @Query("SELECT * FROM ChecklistNotes WHERE idMainNote = :idMainNote")
    suspend fun getByIdMainNote(idMainNote: String): List<ChecklistNoteEntity>

    @Update
    suspend fun update(note: ChecklistNoteEntity)

    @Query("DELETE FROM ChecklistNotes WHERE id = :itemID")
    suspend fun delete(itemID: String)

    @Query("DELETE FROM ChecklistNotes WHERE idMainNote = :idMainNote")
    suspend fun deleteByIdMainNote(idMainNote: String)

    @Query("DELETE FROM ChecklistNotes")
    suspend fun deleteAll()
}
