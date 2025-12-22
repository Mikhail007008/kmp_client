package com.example.kmp_client.core.imageUtils

actual class ImagePicker actual constructor(activity: ComponentActivity) {
    actual suspend fun pickImage(source: ImageSource): ImageProcessingResult? {
        return null
    }
}