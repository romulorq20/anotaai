package br.com.rrrqueiroz.notas.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.rrrqueiroz.notas.R
import br.com.rrrqueiroz.notas.domain.model.Note
import br.com.rrrqueiroz.notas.domain.model.NoteType
import br.com.rrrqueiroz.notas.extensions.toDisplayDate
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddNewNote: (String?) -> Unit = {},
    onOpenNote: (String) -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isFabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HomeTopAppBar(onOpenProfile = onOpenProfile)
            },
            content = { paddingValues ->
                HomeContent(
                    state = state,
                    paddingValues = paddingValues,
                    onOpenNote = onOpenNote,
                    onNoteIntent = viewModel::handleIntent
                )

                state.itemToDelete?.let { note ->
                    DeleteNoteDialog(
                        onConfirm = {
                            viewModel.handleIntent(HomeIntent.DeleteNote(note))
                            viewModel.handleIntent(HomeIntent.SetItemToDelete(null))
                        },
                        onDismiss = {
                            viewModel.handleIntent(HomeIntent.SetItemToDelete(null))
                        }
                    )
                }
            }
        )

        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable { isFabExpanded = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            ExpandableAddNoteFAB(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onOptionClick = { type ->
                    isFabExpanded = false
                    onAddNewNote(type)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(onOpenProfile: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        actions = {
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Default.Settings, contentDescription = "Configurações")
            }
        }
    )
}

@Composable
private fun ExpandableAddNoteFAB(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOptionClick: (String?) -> Unit
) {
    val rotation by animateFloatAsState(if (isExpanded) 45f else 0f, label = "fab_rotation")
    val containerColor by animateColorAsState(
        if (isExpanded) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer,
        label = "fab_color"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                FABOption(icon = Icons.Default.Mic, label = "Áudio", onClick = { onOptionClick(NoteType.AUDIO.name) })
                FABOption(icon = Icons.Default.Image, label = "Imagem", onClick = { onOptionClick(NoteType.IMAGE.name) })
                FABOption(icon = Icons.Default.FormatListBulleted, label = "Lista", onClick = { onOptionClick(NoteType.LIST.name) })
                FABOption(icon = Icons.Default.CameraAlt, label = "Câmera", onClick = { onOptionClick("CAMERA") })
                FABOption(icon = Icons.AutoMirrored.Filled.TextSnippet, label = "Texto", onClick = { onOptionClick(NoteType.TEXT.name) })
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = containerColor,
            contentColor = contentColorFor(containerColor),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Menu",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun FABOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    paddingValues: PaddingValues,
    onOpenNote: (String) -> Unit,
    onNoteIntent: (HomeIntent) -> Unit
) {
    if (state.notes.isEmpty()) {
        EmptyNotesView(paddingValues)
    } else {
        NotesListView(
            notes = state.notes,
            playingNoteId = state.playingNoteId,
            paddingValues = paddingValues,
            onOpenNote = onOpenNote,
            onNoteIntent = onNoteIntent
        )
    }
}

@Composable
private fun EmptyNotesView(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = R.mipmap.ic_launcher_foreground,
                contentDescription = null,
                modifier = Modifier.size(180.dp).alpha(0.3f)
            )
            Text(
                text = "Organize suas ideias aqui",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotesListView(
    notes: List<Note>,
    playingNoteId: String?,
    paddingValues: PaddingValues,
    onOpenNote: (String) -> Unit,
    onNoteIntent: (HomeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = 100.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                isPlaying = playingNoteId == note.id,
                onClick = { onOpenNote(note.id) },
                onLongPress = { onNoteIntent(HomeIntent.SetItemToDelete(note)) },
                onToggleAudio = {
                    if (playingNoteId == note.id) {
                        onNoteIntent(HomeIntent.StopAudio)
                    } else {
                        note.thumbnail?.let { path ->
                            onNoteIntent(HomeIntent.ToggleAudio(note.id, path))
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(note.id) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            NoteItemHeader(note, isPlaying, onToggleAudio)
            if (note.type == NoteType.IMAGE) NoteImagePreview(note.thumbnail)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.date.toDisplayDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteItemHeader(note: Note, isPlaying: Boolean, onToggleAudio: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = note.title.ifBlank { "Sem título" },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (note.type == NoteType.AUDIO) {
            IconButton(onClick = onToggleAudio) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun NoteImagePreview(thumbnail: String?) {
    Spacer(modifier = Modifier.height(12.dp))
    AsyncImage(
        model = thumbnail,
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun DeleteNoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir nota?") },
        text = { Text("Deseja apagar permanentemente esta nota?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Excluir", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
