package br.com.rrrqueiroz.notas.extensions

import java.text.SimpleDateFormat
import java.util.Locale

fun Long.toDisplayDate(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(java.util.Date(this))
}
