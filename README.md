# AnotaAI — Notas Multimídia com Clean Architecture + MVI

![copilot-banner](https://github.com/user-attachments/assets/469d2d6e-5f9b-4e19-8d08-cfc93ed09945)

**AnotaAI** é um aplicativo Android de gerenciamento de notas multimídia desenvolvido com as práticas mais modernas do ecossistema Android. O projeto nasceu como um exercício de refatoração sobre uma base de curso e evoluiu para uma demonstração aplicada de **Clean Architecture**, **MVI** e **Jetpack** — servindo tanto como portfólio técnico quanto como material de referência para quem quer entender essas arquiteturas na prática.

---

## O que o app faz

O usuário pode criar, visualizar e excluir notas de quatro tipos diferentes:

| Tipo | O que é salvo |
|---|---|
| **Texto** | Conteúdo escrito livremente |
| **Áudio** | Gravação de voz diretamente no app |
| **Imagem** | Foto tirada pela câmera ou selecionada da galeria |
| **Lista** | Checklist com itens marcáveis |

Todas as notas ficam salvas localmente no dispositivo usando o banco de dados Room. A tela principal lista todas as notas com preview, data de criação e controles de reprodução para notas de áudio. Existe também uma tela de configurações que mostra o total de notas e permite apagar tudo de uma vez.

---

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Arquitetura | Clean Architecture + MVI |
| Banco de dados | Room |
| Injeção de dependência | Dagger Hilt |
| Navegação | Compose Navigation (type-safe com `@Serializable`) |
| Imagens | Coil 3 |
| Assincronismo | Kotlin Coroutines + Flow |
| Câmera | CameraX |
| Testes | JUnit 4 + MockK + Turbine |

---

## Estrutura do projeto

```
br.com.rrrqueiroz.notas/
├── domain/              ← Regras de negócio puras
│   ├── model/
│   └── usecase/
├── data/                ← Acesso a dados
│   ├── datasource/
│   ├── mapper/
│   └── repository/
├── presentation/        ← UI e estado
│   ├── home/
│   ├── notes/
│   ├── settings/
│   ├── camera/
│   └── navigation/
├── di/                  ← Módulos Hilt
└── utils/               ← Utilitários (AudioManager, FileUtils…)
```

---

## Arquitetura: Clean Architecture

O projeto separa o código em três camadas com regras claras de dependência: **a seta de dependência aponta sempre para dentro**, ou seja, a camada externa conhece a interna, nunca o contrário.

```
┌─────────────────────────────────┐
│        Presentation             │  ← Compose, ViewModels, MVI
├─────────────────────────────────┤
│            Data                 │  ← Room, DataSources, Mappers
├─────────────────────────────────┤
│           Domain                │  ← Modelos, UseCases, Interfaces
└─────────────────────────────────┘
        ↑ dependências apontam para cá
```

### Camada Domain

É o núcleo do app. Não tem nenhuma dependência de framework Android — é Kotlin puro. Aqui vivem:

**Modelos de domínio** — representam os dados como o negócio os enxerga, não como o banco os armazena:

```kotlin
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: Long = System.currentTimeMillis(),
    val listItems: List<BaseNote> = emptyList(),
    val thumbnail: String? = null,
    val type: NoteType = NoteType.TEXT
)
```

Uma nota é uma composição: ela tem um tipo principal (`NoteType`) e uma lista de itens (`BaseNote`), que podem ser texto, imagem, áudio ou checklist. Esse design permite que uma nota evolua sem quebrar o contrato.

**Use Cases** — cada operação do negócio tem sua classe, seguindo o princípio de responsabilidade única:

```kotlin
// Busca uma nota por ID
class GetNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String): Note? = repository.getNoteById(id)
}
```

```kotlin
// Salva uma nota (cria ou atualiza)
class SaveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.saveNote(note)
}
```

**Interface do repositório** — o domínio define o contrato, mas não sabe como ele é implementado:

```kotlin
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun removeNote(note: Note)
    suspend fun countNotes(): Int
    suspend fun deleteAllNotes()
    suspend fun removeItemNote(noteItem: BaseNote)
}
```

### Camada Data

Implementa os contratos definidos no domínio. É aqui que o Room entra.

**Entidades** são a representação dos dados no banco — diferentes dos modelos de domínio por design:

```kotlin
@Entity(tableName = "Notes")
data class NoteEntity(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: Long = System.currentTimeMillis(),
    val thumbnail: String? = null,
    val type: String = NoteType.TEXT.name
)
```

**Mapper** converte entidade ↔ modelo de domínio, mantendo as camadas isoladas:

```kotlin
fun NoteEntity.toNote() = Note(
    id = id,
    title = title,
    date = date,
    thumbnail = thumbnail,
    type = NoteType.valueOf(type)
)
```

**DataSource** abstrai o acesso ao Room com uma interface própria, permitindo que a implementação seja substituída (e facilitando testes):

```kotlin
interface NoteLocalDataSource {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun saveNote(note: Note)
    // ...
}

class RoomNoteDataSource @Inject constructor(
    private val noteDao: NoteDao,
    // ...
) : NoteLocalDataSource { ... }
```

**Repository** implementa a interface do domínio coordenando os DataSources:

```kotlin
class NoteRepositoryImpl @Inject constructor(
    private val localDataSource: NoteLocalDataSource
) : NoteRepository {
    override fun getAllNotes() = localDataSource.getAllNotes()
    // ...
}
```

### Camada Presentation

Construída com Jetpack Compose e o padrão **MVI**. Cada tela tem quatro arquivos com responsabilidades bem definidas.

---

## Padrão MVI (Model-View-Intent)

MVI é um padrão de gerenciamento de estado unidirecional. O fluxo segue sempre a mesma direção:

```
Usuário age → Intent → ViewModel → State → UI renderiza
                                 ↘ Effect → UI reage (navegação, snackbar)
```

A grande vantagem é previsibilidade: dado um estado, a UI sempre renderiza da mesma forma. Dado um intent, o ViewModel sempre produz o mesmo estado.

### Os quatro arquivos por tela

Veja como isso se aplica na tela de criação de notas (`notes/`):

**`NoteIntent.kt`** — todas as ações que o usuário pode disparar:

```kotlin
sealed class NoteIntent {
    data class LoadNote(val noteId: String) : NoteIntent()
    object SaveNote : NoteIntent()
    data class UpdateTitle(val title: String) : NoteIntent()
    data class AddImage(val imageLink: String) : NoteIntent()
    object StartRecording : NoteIntent()
    object StopRecording : NoteIntent()
    data class PlayAudio(val path: String) : NoteIntent()
    // ...
}
```

**`NoteUiState.kt`** — snapshot completo do que a UI precisa para se renderizar:

```kotlin
data class NoteUiState(
    val note: Note = Note(),
    val noteTextAppBar: String = "Nova nota ",
    val noteText: String = "",
    val showCameraScreen: Boolean = false,
    val isRecording: Boolean = false,
    val audioDuration: Int = 0,
    val audioPath: String = ""
)
```

**`NoteEffect.kt`** — eventos de única ocorrência que não são estado (ex: navegar para outra tela ou mostrar um erro):

```kotlin
sealed class NoteEffect {
    object NavigateBack : NoteEffect()
    data class ShowError(val message: String) : NoteEffect()
}
```

**`NoteViewModel.kt`** — o cérebro: recebe intents, processa a lógica, emite estado e efeitos:

```kotlin
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val getNoteUseCase: GetNoteUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val repository: NoteRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<NoteEffect>()
    val effect = _effect.asSharedFlow()

    fun handleIntent(intent: NoteIntent) {
        when (intent) {
            NoteIntent.SaveNote -> saveNote()
            NoteIntent.StartRecording -> startRecording()
            is NoteIntent.UpdateTitle -> _uiState.update { it.copy(noteTextAppBar = intent.title) }
            // ...
        }
    }
}
```

**`NoteScreen.kt`** — coleta estado e efeitos, delega tudo ao ViewModel:

```kotlin
@Composable
fun NoteScreen(onBack: () -> Unit = {}) {
    val viewModel = hiltViewModel<NoteViewModel>()
    val state by viewModel.uiState.collectAsState()

    // Coleta efeitos de única ocorrência
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteEffect.NavigateBack -> onBack()
                is NoteEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // UI é uma função pura do estado
    Scaffold { ... }
}
```

> **Por que `StateFlow` para estado e `SharedFlow` para efeitos?**
> `StateFlow` mantém o último valor e o entrega imediatamente para novos coletores — perfeito para estado de UI que precisa sobreviver à recomposição. `SharedFlow` não armazena valor, então um efeito como "navegar para outra tela" não é reemitido se o Composable recompõe.

---

## AudioManager — isolando responsabilidade de mídia

Antes da refatoração, a lógica de `MediaRecorder` e `MediaPlayer` vivia dentro da `MainActivity` e era passada como callbacks por toda a hierarquia de Composables. Isso violava o Single Responsibility Principle e tornava o código difícil de testar.

A solução foi extrair para um singleton injetável:

```kotlin
@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    @Throws(IOException::class)
    fun startRecording(): String {
        val path = "${context.externalCacheDir?.absolutePath}/audio${System.currentTimeMillis()}.acc"
        // configura e inicia o MediaRecorder
        return path
    }

    fun stopRecording() { recorder?.apply { stop(); release() }; recorder = null }

    @Throws(IOException::class)
    fun startPlaying(filePath: String) {
        stopPlaying()
        player = MediaPlayer().apply { setDataSource(filePath); prepare(); start() }
    }

    fun stopPlaying() { player?.release(); player = null }
}
```

O `NoteViewModel` e o `HomeViewModel` injetam o `AudioManager` e chamam seus métodos diretamente. Erros de I/O são capturados e transformados em `Effect.ShowError`, chegando à UI via Snackbar sem nenhum `Toast` direto.

---

## Injeção de Dependência com Hilt

Os módulos Hilt conectam as interfaces às implementações sem que nenhuma camada precise conhecer a outra concretamente:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(
        localDataSource: NoteLocalDataSource
    ): NoteRepository = NoteRepositoryImpl(localDataSource)
}
```

O fluxo de injeção completo:

```
Room (DatabaseModule)
  └→ DAOs
       └→ RoomNoteDataSource (DataSourceModule)
            └→ NoteRepositoryImpl (RepositoryModule)
                 └→ Use Cases (injeção direta via @Inject constructor)
                      └→ ViewModels (@HiltViewModel)
```

---

## Navegação type-safe

As rotas usam `@Serializable` do Kotlin, eliminando o uso de strings mágicas e garantindo que os argumentos sejam verificados em tempo de compilação:

```kotlin
sealed class NoteRoutes {
    @Serializable
    data object Home : NoteRoutes()

    @Serializable
    data class NoteDetail(
        val noteId: String? = null,      // null = criar nova nota
        val initialType: String? = null  // define o tipo inicial
    ) : NoteRoutes()

    @Serializable
    data object Settings : NoteRoutes()
}
```

---

## Testes

Os testes cobrem os ViewModels isoladamente com mocks, sem precisar de Android ou banco de dados:

```kotlin
@Test
fun `when StartRecording fails then should emit ShowError effect`() = runTest {
    every { audioManager.startRecording() } throws IOException("Sem permissão")

    viewModel.effect.test {             // Turbine: coleta o Flow de efeitos
        viewModel.handleIntent(NoteIntent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(NoteEffect.ShowError("Erro ao iniciar gravação"), awaitItem())
    }
}
```

**Ferramentas de teste:**
- **MockK** — mocking idiomático para Kotlin
- **Turbine** — biblioteca do Cash App para testar `Flow` com uma API simples (`test { awaitItem() }`)
- **Coroutines Test** — `StandardTestDispatcher` para controlar o tempo das coroutines e testar comportamentos assíncronos de forma determinística

Para rodar os testes:

```bash
./gradlew test
```

---

## Conformidade com Android 15

O projeto está configurado para suportar dispositivos com **16 KB de page size**, requisito do Google Play para apps publicados em 2025:

```kotlin
// app/build.gradle.kts
ndk {
    abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
}
packaging {
    jniLibs { useLegacyPackaging = true }
}
```

---

## Como rodar

1. Clone o repositório
2. Abra no **Android Studio Ladybug** ou superior
3. Aguarde o Gradle Sync e a geração de código pelo KSP (Hilt + Room)
4. Execute em um emulador ou dispositivo físico com **Android 7.0+** (API 24)

**Requisitos:**
- Android Studio Ladybug+
- JDK 11
- NDK 26.1.10909125
- CMake 3.22.1

---

## Estrutura de arquivos completa

```
app/src/main/java/br/com/rrrqueiroz/notas/
│
├── domain/
│   ├── model/
│   │   ├── BaseNote.kt          ← Classe base abstrata + enum NoteType
│   │   ├── Note.kt              ← Modelo principal de nota
│   │   ├── NoteItemText.kt
│   │   ├── NoteItemAudio.kt
│   │   ├── NoteItemImage.kt
│   │   └── NoteItemChecklist.kt
│   ├── repository/
│   │   └── NoteRepository.kt    ← Interface do repositório
│   └── usecase/
│       ├── GetNoteUseCase.kt
│       ├── SaveNoteUseCase.kt
│       └── DeleteNoteUseCase.kt
│
├── data/
│   ├── datasource/local/
│   │   ├── NoteLocalDataSource.kt   ← Interface
│   │   ├── RoomNoteDataSource.kt    ← Implementação
│   │   └── database/
│   │       ├── AppDatabase.kt
│   │       ├── NoteDao.kt
│   │       ├── entities/
│   │       │   ├── NoteEntity.kt
│   │       │   └── ...
│   │       └── ...
│   ├── mapper/
│   │   └── NoteMapper.kt
│   └── repository/
│       └── NoteRepositoryImpl.kt
│
├── di/module/
│   ├── DatabaseModule.kt
│   ├── DataSourceModule.kt
│   ├── RepositoryModule.kt
│   └── FileUtilsModule.kt
│
├── presentation/
│   ├── MainActivity.kt
│   ├── home/
│   │   ├── HomeIntent.kt
│   │   ├── HomeUiState.kt
│   │   ├── HomeEffect.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeScreen.kt
│   ├── notes/
│   │   ├── NoteIntent.kt
│   │   ├── NoteUiState.kt
│   │   ├── NoteEffect.kt
│   │   ├── NoteViewModel.kt
│   │   ├── NoteScreen.kt
│   │   └── ListNotes.kt
│   ├── settings/
│   │   ├── SettingsIntent.kt
│   │   ├── SettingsUiState.kt
│   │   ├── SettingsEffect.kt
│   │   ├── SettingsViewModel.kt
│   │   └── SettingsScreen.kt
│   ├── camera/
│   ├── navigation/
│   │   ├── NavHost.kt
│   │   └── NoteRoutes.kt
│   └── theme/
│
└── utils/
    ├── AudioManager.kt
    ├── FileUtils.kt
    └── PermissionUtils.kt
```

---

*Projeto desenvolvido por [Ruan Queiroz](https://github.com/rqueiroz) como demonstração de arquitetura Android moderna.*
