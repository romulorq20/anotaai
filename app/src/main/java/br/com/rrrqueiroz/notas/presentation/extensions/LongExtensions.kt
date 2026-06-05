package br.com.rrrqueiroz.notas.presentation.extensions

import java.text.SimpleDateFormat
import java.util.Locale

fun Long.toDisplayDate(): String {
    val date = this
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}