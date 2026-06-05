package br.com.rrrqueiroz.notas.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okio.use
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class FileUtils @Inject constructor(private val context: Context) {
    suspend fun saveOnInternalStorage(
        inputStream: InputStream,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        val folderName = "media"
        val path = context.getExternalFilesDir(folderName)
        val fileName = "file_${System.currentTimeMillis()}"
        val newFile = File(path, fileName)

        withContext(IO) {
            newFile.outputStream().use { file ->
                inputStream.copyTo(file)
            }

            if (newFile.exists()) {
                onSuccess(newFile.path)
            } else {
                onFailure()
            }
        }
    }

    fun saveAudioInternalStorage(
        byteArray: ByteArray
    ): Pair<String, Int> {
        val folderName = "audio"
        val path = context.getExternalFilesDir(folderName)
        val fileName = "audio_${System.currentTimeMillis()}.mp3"
        val audioFile = File(path, fileName)
        audioFile.writeBytes(byteArray)
        val audioPath = audioFile.path

        val durationInSeconds = getAudioDuration(audioPath)

        return Pair(audioPath, durationInSeconds)
    }

    private fun getAudioDuration(audioPath: String): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audioPath)
        val durationStrIMilliseconds =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationInSeconds = durationStrIMilliseconds?.toInt()?.div(1000)
        retriever.release()

        return durationInSeconds ?: 0
    }
}