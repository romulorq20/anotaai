package br.com.rrrqueiroz.notas.presentation.home

import app.cash.turbine.test
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.usecase.DeleteNoteUseCase
import br.com.rrrqueiroz.notas.domain.usecase.GetAllNotesUseCase
import br.com.rrrqueiroz.notas.utils.AudioManager
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
    private val getAllNotesUseCase: GetAllNotesUseCase = mockk()
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk(relaxed = true)
    private val audioManager: AudioManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getAllNotesUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando inicializar deve carregar as notas`() = runTest {
        val notes = listOf(Note(title = "Nota 1"), Note(title = "Nota 2"))
        every { getAllNotesUseCase() } returns flowOf(notes)

        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(notes, awaitItem().notes)
        }
    }

    @Test
    fun `quando deletar nota deve chamar DeleteNoteUseCase`() = runTest {
        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)
        val note = Note(title = "Nota para deletar")

        viewModel.handleIntent(HomeIntent.DeleteNote(note))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteNoteUseCase(note) }
    }

    @Test
    fun `quando definir item para deletar deve atualizar estado`() = runTest {
        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)
        val note = Note(title = "Nota para deletar")

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
    fun `quando reproduzir áudio deve iniciar player e atualizar estado`() = runTest {
        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)

        viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
        testDispatcher.scheduler.advanceUntilIdle()

        verify { audioManager.startPlaying("/path/audio.acc") }
        viewModel.uiState.test {
            assertEquals("1", awaitItem().playingNoteId)
        }
    }

    @Test
    fun `quando reproduzir a mesma nota deve parar o player`() = runTest {
        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)

        viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
        viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
        testDispatcher.scheduler.advanceUntilIdle()

        verify { audioManager.stopPlaying() }
        viewModel.uiState.test {
            assertNull(awaitItem().playingNoteId)
        }
    }

    @Test
    fun `quando falhar ao reproduzir deve emitir efeito ShowMessage`() = runTest {
        every { audioManager.startPlaying(any()) } throws IOException("Arquivo inválido")
        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)

        viewModel.effect.test {
            viewModel.handleIntent(HomeIntent.ToggleAudio("1", "/path/audio.acc"))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(HomeEffect.ShowMessage("Erro ao reproduzir áudio"), awaitItem())
        }
    }

    @Test
    fun `quando parar áudio deve chamar stopPlaying e limpar estado`() = runTest {
        viewModel = HomeViewModel(getAllNotesUseCase, deleteNoteUseCase, audioManager)

        viewModel.handleIntent(HomeIntent.StopAudio)

        verify { audioManager.stopPlaying() }
        viewModel.uiState.test {
            assertNull(awaitItem().playingNoteId)
        }
    }
}
