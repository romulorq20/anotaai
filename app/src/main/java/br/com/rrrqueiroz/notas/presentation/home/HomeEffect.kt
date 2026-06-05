package br.com.rrrqueiroz.notas.presentation.home

sealed class HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect()
}
