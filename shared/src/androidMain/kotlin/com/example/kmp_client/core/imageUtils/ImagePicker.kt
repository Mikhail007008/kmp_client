/**
 * Android реализация ImagePicker
 * Обрабатывает выбор фото из галереи или камеры, подготавливает изображения
 */
package com.example.kmp_client.core.imageUtils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.scale


internal class AndroidImagePickerEngine(
    private val activity: ComponentActivity,
    private val pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    private val takePictureLauncher: ActivityResultLauncher<Uri>,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {
    private var imageResultDeferred: CompletableDeferred<ImageProcessingResult?>? = null
    private var tempCameraUri: Uri? = null

    suspend fun pickImage(source: ImageSource): ImageProcessingResult? {
        imageResultDeferred = CompletableDeferred()

        when (source) {
            ImageSource.GALLERY -> {
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            ImageSource.CAMERA -> {
                if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    launchCameraWithUri()
                } else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        return imageResultDeferred?.await()
    }

    private fun launchCameraWithUri() {
        tempCameraUri = createImageFileUri()
        if (tempCameraUri != null) {
            takePictureLauncher.launch(tempCameraUri)
        } else imageResultDeferred?.complete(null)
    }

    fun onGalleryResult(uri: Uri?) {
        processUri(uri)
    }

    fun onCameraResult(success: Boolean) {
        if (success && tempCameraUri != null) {
            processUri(tempCameraUri)
        } else imageResultDeferred?.complete(ImageProcessingResult.Cancelled)

        tempCameraUri = null
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) launchCameraWithUri()
        else imageResultDeferred?.complete(ImageProcessingResult.Error("Разрешение на использование камеры не предоставлено."))
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun processUri(uri: Uri?) {
        if (uri == null) {
            imageResultDeferred?.complete(ImageProcessingResult.Cancelled)
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val byteArray = uriToByteArrayConverter(activity, uri)
                imageResultDeferred?.complete(byteArray)
            } catch (e: Exception) {
                e.printStackTrace()
                imageResultDeferred?.complete(ImageProcessingResult.Error("Ошибка обработки изображения"))
            }
        }
    }

    private fun createImageFileUri(): Uri? {
        val context = activity

        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uriToByteArrayConverter(context: Context, uri: Uri): ImageProcessingResult {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            if (bitmap.width < ImageProcessingConfig.MIN_IMAGE_WIDTH || bitmap.height < ImageProcessingConfig.MIN_IMAGE_HEIGHT) {
                return ImageProcessingResult.Error("Изображение слишком маленькое. Минимальный размер: ${ImageProcessingConfig.MIN_IMAGE_WIDTH}x${ImageProcessingConfig.MIN_IMAGE_HEIGHT}px.")
            }

            val scaledBitmap = scaleBitmap(
                bitmap,
                ImageProcessingConfig.MAX_IMAGE_WIDTH,
                ImageProcessingConfig.MAX_IMAGE_HEIGHT
            )
            ByteArrayOutputStream().use { outputStream ->
                scaledBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    ImageProcessingConfig.JPEG_COMPRESSION_QUALITY,
                    outputStream
                )
                val byteArray = outputStream.toByteArray()

                if (byteArray.size > ImageProcessingConfig.MAX_FILE_SIZE_BYTES) {
                    val actualSizeMb = byteArray.size / (1024.0 * 1024.0)
                    val maxSizeMb = ImageProcessingConfig.MAX_FILE_SIZE_BYTES / (1024.0 * 1024.0)
                    return ImageProcessingResult.Error(
                        "Файл слишком большой (%.2f МБ). Максимальный размер: %.2f МБ.".format(actualSizeMb, maxSizeMb)
                    )
                }
                return ImageProcessingResult.Success(byteArray, scaledBitmap.width, scaledBitmap.height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ImageProcessingResult.Error("Не удалось обработать изображение")
        }
    }

    private fun scaleBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (source.width <= maxWidth && source.height <= maxHeight) {
            return source
        }

        val ratioX = maxWidth.toDouble() / source.width
        val ratioY = maxHeight.toDouble() / source.height
        val ratio = kotlin.math.min(ratioX, ratioY)

        val newWidth = (source.width * ratio).toInt()
        val newHeight = (source.height * ratio).toInt()

        return source.scale(newWidth, newHeight)
    }
}

actual class ImagePicker {
    internal var engine: AndroidImagePickerEngine? = null

    actual suspend fun pickImage(source: ImageSource): ImageProcessingResult? {
        return engine?.pickImage(source)
    }
}

@Composable
fun rememberActualImagePicker(): ImagePicker {
    val activity = LocalContext.current as ComponentActivity
    val imagePickerInstanse = remember { ImagePicker() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            imagePickerInstanse.engine?.onGalleryResult(uri)
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            imagePickerInstanse.engine?.onCameraResult(success)
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            imagePickerInstanse.engine?.onPermissionResult(isGranted)
        }
    )

    val currentEngine = remember(activity, pickMediaLauncher, takePictureLauncher, requestPermissionLauncher) {
        AndroidImagePickerEngine(
            activity = activity,
            pickMediaLauncher = pickMediaLauncher,
            takePictureLauncher = takePictureLauncher,
            requestPermissionLauncher = requestPermissionLauncher
        )
    }
    imagePickerInstanse.engine = currentEngine

    return imagePickerInstanse
}

object ImageProcessingConfig {
    const val MAX_FILE_SIZE_BYTES = 8 * 1024 * 1024
    const val MAX_IMAGE_WIDTH = 1920
    const val MAX_IMAGE_HEIGHT = 1080
    const val JPEG_COMPRESSION_QUALITY = 75
    const val MIN_IMAGE_WIDTH = 100
    const val MIN_IMAGE_HEIGHT = 100
}