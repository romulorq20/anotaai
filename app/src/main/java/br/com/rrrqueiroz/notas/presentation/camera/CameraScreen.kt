package br.com.rrrqueiroz.notas.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import br.com.rrrqueiroz.notas.R
import br.com.rrrqueiroz.notas.extensions.rotateBitmap
import br.com.rrrqueiroz.notas.extensions.saveBitmapToInternalStorage
import java.util.concurrent.Executor

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScreen(
    onImageSaved: (String) -> Unit = {},
    onError: () -> Unit = {}
) {
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current
        val cameraController = remember {
            LifecycleCameraController(context).apply {
                this.cameraSelector = cameraSelector
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            CameraPreview(cameraController)
            Row(
                modifier = Modifier
                    .padding(bottom = 56.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.clickable {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                        cameraController.cameraSelector = cameraSelector
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Switch Camera",
                        )
                        Text(
                            "Switch",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
                Card(
                    modifier = Modifier.clickable {
                        capturePhoto(context, cameraController) { bitmap ->
                            capturedImage = bitmap
                        }
                    },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.ic_camera),
                        contentDescription = "Câmera",
                    )
                    Text(
                        stringResource(R.string.take_photo),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
        }

        capturedImage?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    it,
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 56.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Card(
                        onClick = { capturedImage = null }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                            )
                            Text(
                                stringResource(R.string.take_another),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    Card(
                        onClick = {
                            capturedImage?.let {
                                it.saveBitmapToInternalStorage(
                                    context,
                                    onSaved = { filePath ->
                                        onImageSaved(filePath)
                                        capturedImage = null
                                    },
                                    onError = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                            .show()
                                        onError()
                                    }
                                )
                            }
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ok",
                            )
                            Text(
                                stringResource(R.string.user_photo),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

}

private fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val correctedBitmap: Bitmap = image
                .toBitmap()
                .rotateBitmap(image.imageInfo.rotationDegrees)
            onPhotoCaptured(correctedBitmap)
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraContent", "Error ao capturar imagem", exception)
        }
    })
}
