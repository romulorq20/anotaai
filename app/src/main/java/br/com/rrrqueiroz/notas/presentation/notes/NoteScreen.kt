package br.com.rrrqueiroz.notas.presentation.notes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.rrrqueiroz.notas.domain.model.NoteType
import br.com.rrrqueiroz.notas.presentation.camera.CameraInitializer
import br.com.rrrqueiroz.notas.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    noteToEdit: String? = null,
    initialType: String? = null,
    onBack: () -> Unit = {}
) {
    val viewModel = hiltViewModel<NoteViewModel>()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                PermissionUtils(context).persistUriPermission(it)
                viewModel.handleIntent(NoteIntent.AddImage(it.toString()))
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteEffect.NavigateBack -> onBack()
                is NoteEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (noteToEdit != null) {
            viewModel.handleIntent(NoteIntent.LoadNote(noteToEdit))
        } else {
            viewModel.handleIntent(NoteIntent.ResetNote)

            when (initialType) {
                "CAMERA" -> {
                    viewModel.handleIntent(NoteIntent.InitializeNoteType(NoteType.IMAGE))
                    viewModel.handleIntent(NoteIntent.ToggleCamera(true))
                }
                NoteType.IMAGE.name -> {
                    viewModel.handleIntent(NoteIntent.InitializeNoteType(NoteType.IMAGE))
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                NoteType.LIST.name -> viewModel.handleIntent(NoteIntent.InitializeNoteType(NoteType.LIST))
                NoteType.AUDIO.name -> viewModel.handleIntent(NoteIntent.InitializeNoteType(NoteType.AUDIO))
                NoteType.TEXT.name -> viewModel.handleIntent(NoteIntent.InitializeNoteType(NoteType.TEXT))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteToEdit == null) "Criar Nota" else "Editar nota",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.handleIntent(NoteIntent.SaveNote) }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Salvar")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                BasicTextField(
                    value = state.noteTextAppBar,
                    onValueChange = { viewModel.handleIntent(NoteIntent.UpdateTitle(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (state.noteTextAppBar.isEmpty()) {
                            Text(
                                text = "Título",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ListNotes(
                    modifier = Modifier.weight(1f),
                    noteState = state.note,
                    noteText = state.noteText,
                    isRecording = state.isRecording,
                    audioDuration = state.audioDuration,
                    onStartRecording = { viewModel.handleIntent(NoteIntent.StartRecording) },
                    onStopRecording = { viewModel.handleIntent(NoteIntent.StopRecording) },
                    onPlayAudio = { path -> viewModel.handleIntent(NoteIntent.PlayAudio(path)) },
                    onStopAudio = { viewModel.handleIntent(NoteIntent.StopAudio) },
                    onUpdatedItem = { updateItem, id ->
                        viewModel.handleIntent(NoteIntent.UpdateItemText(id, updateItem))
                    },
                    onDeletedItem = { itemNote ->
                        viewModel.handleIntent(NoteIntent.DeleteItem(itemNote))
                    },
                    onUpdateChecklist = { id, text, checked ->
                        viewModel.handleIntent(NoteIntent.UpdateChecklistItem(id, text, checked))
                    },
                    onAddChecklistItem = { viewModel.handleIntent(NoteIntent.AddChecklistItem) }
                )
            }
        }
    )

    if (state.showCameraScreen) {
        CameraInitializer(
            onImageSaved = { filePath ->
                viewModel.handleIntent(NoteIntent.AddImage(filePath))
                viewModel.handleIntent(NoteIntent.ToggleCamera(false))
            },
            onError = {
                viewModel.handleIntent(NoteIntent.ToggleCamera(false))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNoteScreen() {
    NoteScreen()
}
