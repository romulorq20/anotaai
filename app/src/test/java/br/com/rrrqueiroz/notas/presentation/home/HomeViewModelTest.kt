package br.com.rrrqueiroz.notas.presentation.home

import app.cash.turbine.test
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.repository.NoteRepository
import br.com.rrrqueiroz.notas.utils.AudioManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val repository: NoteRepository = mockk(relaxed = true)
    private val audioManager: AudioManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllNotes() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when init then should load notes`() = runTest {
        val notes = listOf(Note(title = "Note 1"), Note(title = "Note 2"))
        every { repository.getAllNotes() } returns flowOf(notes)

        viewModel = HomeViewModel(repository, audioManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(notes, awaitItem().notes)
        }
    }

    @Test
    fun `when handle DeleteNote intent then should call repository`() = runTest {
        viewModel = HomeViewModel(repository, audioManager)
        val note = Note(title = "Note to delete")

        viewModel.handleIntent(HomeIntent.DeleteNote(note))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.removeNote(note) }
    }

    @Test
    fun `when handle SetItemToDelete intent then should update uiState`() = runTest {
        viewModel = HomeViewModel(repository, audioManager)
        val note = Note(title = "Note to delete")

        viewModel.handleIntent(HomeIntent.SetItemToDelete(note))

        viewModel.uiState.test {
            assertEquals(note, awaitItem().itemToDelete)
        }

        viewModel.handleIntent(HomeIntent.SetItemToDelete(null))
        viewModel.uiState.test {
            assertNull(awaitItem().itemToDelete)
        }
    }

    @Test
    fun `when handle ToggleAudio then should start playing and update state`() = runTest {
        viewModel = HomeViewModel(repository, audioManager)
        val note = Note(id = "1", title = "Audio note")

        viewModel.handleIntent(HomeIntent.ToggleAudio(note.id, "/path/audio.acc"))
        testDispatcher.scheduler.advanceUntilIdle()

        verify { audioManager.startPlaying("/path/audio.acc") }
        viewModel.uiState.test {
            assertEquals("1", awaitItem().playingNoteId)
        }
    }

    @Test
    fun `when handle ToggleAudio on same note then should stop playing`() = runTest {
        viewModel = HomeViewModel(repository, audioManager)

        viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
        viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
        testDispatcher.scheduler.advanceUntilIdle()

        verify { audioManager.stopPlaying() }
        viewModel.uiState.test {
            assertNull(awaitItem().playingNoteId)
        }
    }

    @Test
    fun `when audio playback fails then should emit ShowMessage effect`() = runTest {
        every { audioManager.startPlaying(any()) } throws IOException("Falha")
        viewModel = HomeViewModel(repository, audioManager)

        viewModel.effect.test {
            viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(HomeEffect.ShowMessage("Erro ao reproduzir áudio"), awaitItem())
        }
    }

    @Test
    fun `when handle StopAudio then should stop playing and clear state`() = runTest {
        viewModel = HomeViewModel(repository, audioManager)

        viewModel.handleIntent(HomeIntent.StopAudio)

        verify { audioManager.stopPlaying() }
        viewModel.uiState.test {
            assertNull(awaitItem().playingNoteId)
        }
    }
}
