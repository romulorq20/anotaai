package br.com.rrrqueiroz.notas.presentation.navigation

import kotlinx.serialization.Serializable

sealed class NoteRoutes {
    @Serializable
    data object Home : NoteRoutes()

    @Serializable
    data class NoteDetail(
        val noteId: String? = null,
        val initialType: String? = null
    ) : NoteRoutes()

    @Serializable
    data object Settings : NoteRoutes()
}
