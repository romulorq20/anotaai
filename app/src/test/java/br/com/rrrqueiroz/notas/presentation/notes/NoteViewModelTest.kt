package br.com.rrrqueiroz.notas.presentation.notes

import app.cash.turbine.test
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.model.NoteItemAudio
import br.com.rrrqueiroz.notas.domain.model.NoteItemImage
import br.com.rrrqueiroz.notas.domain.model.NoteItemText
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import br.com.rrrqueiroz.notas.domain.usecase.DeleteItemNoteUseCase
import br.com.rrrqueiroz.notas.domain.usecase.GetNoteUseCase
import br.com.rrrqueiroz.notas.domain.usecase.SaveNoteUseCase
import br.com.rrrqueiroz.notas.utils.AudioManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private lateinit var viewModel: NoteViewModel
    private val getNoteUseCase: GetNoteUseCase = mockk()
    private val saveNoteUseCase: SaveNoteUseCase = mockk(relaxed = true)
    private val removeItemNoteUseCase: DeleteItemNoteUseCase = mockk(relaxed = true)
    private val audioManager: AudioManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NoteViewModel(getNoteUseCase, saveNoteUseCase, removeItemNoteUseCase, audioManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when handle LoadNote intent then should update state with note`() = runTest {
        val noteId = "1"
        val note = Note(id = noteId, title = "Test Note")
        coEvery { getNoteUseCase(noteId) } returns note

        viewModel.handleIntent(NoteIntent.LoadNote(noteId))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(note, state.note)
            assertEquals("Test Note", state.noteTextAppBar)
        }
    }

    @Test
    fun `when handle LoadNote intent with null result then should not update note state`() = runTest {
        val noteId = "1"
        coEvery { getNoteUseCase(noteId) } returns null

        viewModel.handleIntent(NoteIntent.LoadNote(noteId))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.note.title)
        }
    }

    @Test
    fun `when handle SaveNote intent then should call saveNoteUseCase`() = runTest {
        viewModel.handleIntent(NoteIntent.UpdateTitle("New Title"))
        viewModel.handleIntent(NoteIntent.SaveNote)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveNoteUseCase(match { it.title == "New Title" }) }
    }

    @Test
    fun `when handle DeleteItem intent then should call repository and reload note`() = runTest {
        val noteId = "1"
        val note = Note(id = noteId, title = "Test Note")
        coEvery { getNoteUseCase(noteId) } returns note
        viewModel.handleIntent(NoteIntent.LoadNote(noteId))
        testDispatcher.scheduler.advanceUntilIdle()

        val itemToDelete = NoteItemText(content = "Delete me")
        viewModel.handleIntent(NoteIntent.DeleteItem(itemToDelete))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { removeItemNoteUseCase(itemToDelete) }
        coVerify(exactly = 2) { getNoteUseCase(noteId) }
    }

    @Test
    fun `when handle AddImage intent then should add image item to note`() = runTest {
        val imageLink = "content://image"
        viewModel.handleIntent(NoteIntent.AddImage(imageLink))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.note.listItems.any { it is NoteItemImage && it.link == imageLink })
        }
    }

    @Test
    fun `when handle AddAudio intent then should add audio item and reset addAudioNote flag`() = runTest {
        val audioPath = "/path/to/audio"
        val duration = 10
        viewModel.handleIntent(NoteIntent.SetAudioPath(audioPath))
        viewModel.handleIntent(NoteIntent.UpdateAudioDuration(duration))
        viewModel.handleIntent(NoteIntent.AddAudio)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.note.listItems.any { it is NoteItemAudio && it.link == audioPath && it.duration == duration })
            assertFalse(state.addAudioNote)
        }
    }

    @Test
    fun `when handle AddText intent then should add text item to note`() = runTest {
        viewModel.handleIntent(NoteIntent.UpdateNoteText("Some content"))
        viewModel.handleIntent(NoteIntent.AddText)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.note.listItems.any { it is NoteItemText && it.content == "Some content" })
            assertEquals("", state.noteText)
        }
    }

    @Test
    fun `when handle UpdateItemText intent then should update item in list`() = runTest {
        val itemId = "item1"
        val initialItem = NoteItemText(id = itemId, content = "Initial")
        val note = Note(listItems = listOf(initialItem))
        coEvery { getNoteUseCase(any()) } returns note

        viewModel.handleIntent(NoteIntent.LoadNote("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(NoteIntent.UpdateItemText(itemId, "Updated"))

        viewModel.uiState.test {
            val state = awaitItem()
            val item = state.note.listItems.find { it.id == itemId } as NoteItemText
            assertEquals("Updated", item.content)
        }
    }

    @Test
    fun `when handle ToggleCamera intent then should update showCameraScreen`() = runTest {
        viewModel.handleIntent(NoteIntent.ToggleCamera(true))
        viewModel.uiState.test { assertTrue(awaitItem().showCameraScreen) }

        viewModel.handleIntent(NoteIntent.ToggleCamera(false))
        viewModel.uiState.test { assertFalse(awaitItem().showCameraScreen) }
    }

    @Test
    fun `when handle ToggleRecording intent then should update isRecording`() = runTest {
        viewModel.handleIntent(NoteIntent.ToggleRecording(true))
        viewModel.uiState.test { assertTrue(awaitItem().isRecording) }

        viewModel.handleIntent(NoteIntent.ToggleRecording(false))
        viewModel.uiState.test { assertFalse(awaitItem().isRecording) }
    }

    @Test
    fun `when handle ToggleAudioNote intent then should update addAudioNote`() = runTest {
        viewModel.handleIntent(NoteIntent.ToggleAudioNote(true))
        viewModel.uiState.test { assertTrue(awaitItem().addAudioNote) }

        viewModel.handleIntent(NoteIntent.ToggleAudioNote(false))
        viewModel.uiState.test { assertFalse(awaitItem().addAudioNote) }
    }

    @Test
    fun `when handle UpdateTitle intent then should update appBar text`() = runTest {
        viewModel.handleIntent(NoteIntent.UpdateTitle("Updated Title"))

        viewModel.uiState.test {
            assertEquals("Updated Title", awaitItem().noteTextAppBar)
        }
    }

    @Test
    fun `when handle ResetNote intent then should reset uiState`() = runTest {
        viewModel.handleIntent(NoteIntent.UpdateTitle("Dirty Title"))
        viewModel.handleIntent(NoteIntent.ResetNote)

        viewModel.uiState.test {
            assertEquals("Nova nota ", awaitItem().noteTextAppBar)
        }
    }

    @Test
    fun `when StartRecording succeeds then should update audioPath and isRecording`() = runTest {
        every { audioManager.startRecording() } returns "/path/recorded.acc"

        viewModel.handleIntent(NoteIntent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("/path/recorded.acc", state.audioPath)
            assertTrue(state.isRecording)
        }
    }

    @Test
    fun `when StartRecording fails then should emit ShowError effect`() = runTest {
        every { audioManager.startRecording() } throws IOException("Sem permissão")

        viewModel.effect.test {
            viewModel.handleIntent(NoteIntent.StartRecording)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(NoteEffect.ShowError("Erro ao iniciar gravação"), awaitItem())
        }
    }

    @Test
    fun `when StopRecording then should stop recorder and add audio item`() = runTest {
        every { audioManager.startRecording() } returns "/path/recorded.acc"
        viewModel.handleIntent(NoteIntent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(NoteIntent.StopRecording)

        verify { audioManager.stopRecording() }
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isRecording)
            assertTrue(state.note.listItems.any { it is NoteItemAudio })
        }
    }

    @Test
    fun `when PlayAudio fails then should emit ShowError effect`() = runTest {
        every { audioManager.startPlaying(any()) } throws IOException("Arquivo inválido")

        viewModel.effect.test {
            viewModel.handleIntent(NoteIntent.PlayAudio("/invalid/path"))
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(NoteEffect.ShowError("Erro ao reproduzir áudio"), awaitItem())
        }
    }
}
