package br.com.rrrqueiroz.notas.presentation.settings

sealed class SettingsEffect {
    data class ShowMessage(val message: String) : SettingsEffect()
}
