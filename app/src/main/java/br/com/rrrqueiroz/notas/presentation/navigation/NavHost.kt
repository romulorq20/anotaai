package br.com.rrrqueiroz.notas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import br.com.rrrqueiroz.notas.presentation.home.HomeScreen
import br.com.rrrqueiroz.notas.presentation.notes.NoteScreen
import br.com.rrrqueiroz.notas.presentation.settings.SettingsScreen

@Composable
fun NavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NoteRoutes.Home
    ) {
        composable<NoteRoutes.Home> {
            HomeScreen(
                onAddNewNote = { type ->
                    navController.navigate(NoteRoutes.NoteDetail(initialType = type))
                },
                onOpenNote = { noteId ->
                    navController.navigate(NoteRoutes.NoteDetail(noteId = noteId))
                },
                onOpenProfile = {
                    navController.navigate(NoteRoutes.Settings)
                }
            )
        }

        composable<NoteRoutes.NoteDetail> { backStackEntry ->
            val noteDetail: NoteRoutes.NoteDetail = backStackEntry.toRoute()
            NoteScreen(
                noteToEdit = noteDetail.noteId,
                initialType = noteDetail.initialType,
                onBack = { navController.popBackStack() }
            )
        }

        composable<NoteRoutes.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
