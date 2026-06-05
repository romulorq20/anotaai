package br.com.rrrqueiroz.notas.domain.usecase

import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetNoteUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val useCase = GetNoteUseCase(repository)

    @Test
    fun `when invoke then should return note from repository`() = runTest {
        val noteId = "1"
        val expectedNote = Note(id = noteId, title = "Test Note")
        coEvery { repository.getNoteById(noteId) } returns expectedNote

        val result = useCase(noteId)

        assertEquals(expectedNote, result)
    }

    @Test
    fun `when repository returns null then should return null`() = runTest {
        val noteId = "1"
        coEvery { repository.getNoteById(noteId) } returns null

        val result = useCase(noteId)

        assertEquals(null, result)
    }
}
