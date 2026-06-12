package br.com.rrrqueiroz.notas.presentation.settings

import app.cash.turbine.test
import br.com.rrrqueiroz.notas.domain.usecase.CountNotesUseCase
import br.com.rrrqueiroz.notas.domain.usecase.DeleteAllNotesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val countNotesUseCase: CountNotesUseCase = mockk()
    private val deleteAllNotesUseCase: DeleteAllNotesUseCase = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { countNotesUseCase() } returns 5
        viewModel = SettingsViewModel(countNotesUseCase, deleteAllNotesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando inicializar deve carregar a contagem de notas`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(5, awaitItem().notesCount)
        }
    }

    @Test
    fun `quando carregar contagem deve chamar CountNotesUseCase`() = runTest {
        coEvery { countNotesUseCase() } returns 10

        viewModel.handleIntent(SettingsIntent.LoadNotesCount)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(10, awaitItem().notesCount)
        }
    }

    @Test
    fun `quando exibir dialogo deve atualizar showConfirmDeleteDialog`() = runTest {
        viewModel.handleIntent(SettingsIntent.ShowDeleteDialog(true))
        viewModel.uiState.test {
            assertEquals(true, awaitItem().showConfirmDeleteDialog)
        }

        viewModel.handleIntent(SettingsIntent.ShowDeleteDialog(false))
        viewModel.uiState.test {
            assertEquals(false, awaitItem().showConfirmDeleteDialog)
        }
    }

    @Test
    fun `quando apagar todas deve chamar DeleteAllNotesUseCase, zerar contador e emitir ShowMessage`() = runTest {
        viewModel.effect.test {
            viewModel.handleIntent(SettingsIntent.DeleteAllNotes)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { deleteAllNotesUseCase() }

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(0, state.notesCount)
                assertEquals(false, state.showConfirmDeleteDialog)
            }

            assertEquals(SettingsEffect.ShowMessage("Todas as notas foram apagadas"), awaitItem())
        }
    }
}
