package br.com.rrrqueiroz.notas.presentation.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.rrrqueiroz.notas.R
import br.com.rrrqueiroz.notas.domain.model.BaseNote
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.model.NoteItemAudio
import br.com.rrrqueiroz.notas.domain.model.NoteItemChecklist
import br.com.rrrqueiroz.notas.domain.model.NoteItemImage
import br.com.rrrqueiroz.notas.domain.model.NoteItemText
import br.com.rrrqueiroz.notas.domain.model.NoteType
import br.com.rrrqueiroz.notas.extensions.audioDisplay
import coil3.compose.AsyncImage

@Composable
fun ListNotes(
    modifier: Modifier = Modifier,
    noteText: String = "",
    onNoteTextChanged: (String) -> Unit = {},
    noteState: Note = Note(),
    isRecording: Boolean = false,
    audioDuration: Int = 0,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onPlayAudio: (String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onUpdatedItem: (String, String) -> Unit = { _, _ -> },
    onDeletedItem: (BaseNote) -> Unit = {},
    onUpdateChecklist: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onAddChecklistItem: () -> Unit = {}
) {
    val stateList = rememberLazyListState()
    var itemToDelete by remember { mutableStateOf<BaseNote?>(null) }

    LazyColumn(
        modifier = modifier,
        state = stateList,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Interface de Gravação (Apenas para Áudio)
        if (noteState.type == NoteType.AUDIO) {
            item {
                RecordingInterface(
                    isRecording = isRecording,
                    duration = audioDuration,
                    onStart = onStartRecording,
                    onStop = onStopRecording
                )
            }
        }

        // 2. Campo de Nota Principal (Apenas para Texto)
        if (noteState.type == NoteType.TEXT) {
            item {
                BasicTextField(
                    value = noteText,
                    onValueChange = onNoteTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                "Nota",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // 3. Lista de Itens Dinâmicos
        items(noteState.listItems.sortedBy { it.date }, key = { it.id }) { item ->
            when (item.type) {
                NoteType.LIST -> {
                    ItemChecklist(
                        item = item as NoteItemChecklist,
                        onUpdate = { text, checked -> onUpdateChecklist(item.id, text, checked) },
                        onDelete = { itemToDelete = item }
                    )
                }
                NoteType.IMAGE -> {
                    ItemNoteImage(
                        item = item as NoteItemImage,
                        onDeleted = { itemToDelete = item }
                    )
                }
                NoteType.AUDIO -> {
                    ItemNoteAudio(
                        item = item as NoteItemAudio,
                        onPlayAudio = onPlayAudio,
                        onStopAudio = onStopAudio,
                        onDeleted = { itemToDelete = item }
                    )
                }
                NoteType.TEXT -> {
                    // Texto adicional se houver
                    ItemNoteText(
                        item = item as NoteItemText,
                        onUpdated = { onUpdatedItem(it, item.id) },
                        onDeleted = { itemToDelete = item }
                    )
                }
            }
        }

        // 4. Botão de Adicionar Item (Apenas para Listas)
        if (noteState.type == NoteType.LIST) {
            item {
                TextButton(
                    onClick = onAddChecklistItem,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar item")
                }
            }
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(text = stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_item)) },
            confirmButton = {
                Button(onClick = { onDeletedItem(itemToDelete!!); itemToDelete = null }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.not))
                }
            }
        )
    }
}

@Composable
private fun RecordingInterface(
    isRecording: Boolean,
    duration: Int,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isRecording) "Gravando... ${duration.audioDisplay()}" else "Gravar nova nota",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(
                onClick = { if (isRecording) onStop() else onStart() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ItemChecklist(
    item: NoteItemChecklist,
    onUpdate: (String, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onChecked -> onUpdate(item.content, onChecked) }
        )
        BasicTextField(
            value = item.content,
            onValueChange = { newText -> onUpdate(newText, item.isChecked) },
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) 
                        else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
            ),
            decorationBox = { innerTextField ->
                if (item.content.isEmpty()) {
                    Text("Item da lista", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                }
                innerTextField()
            }
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Remover item", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ItemNoteText(item: NoteItemText, onUpdated: (String) -> Unit, onDeleted: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Text(item.content, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun ItemNoteImage(item: NoteItemImage, onDeleted: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        AsyncImage(
            model = item.link,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
private fun ItemNoteAudio(item: NoteItemAudio, onPlayAudio: (String) -> Unit, onStopAudio: () -> Unit, onDeleted: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Áudio salvo - ${item.duration.audioDisplay()}", style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = { onPlayAudio(item.link) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Ouvir")
            }
        }
    }
}
