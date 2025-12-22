package com.example.kmp_client.core.imageUtils

expect class ImagePicker {
    suspend fun pickImage(source: ImageSource): ImageProcessingResult?
}

sealed class ImageProcessingResult {
    data class Success(val bytes: ByteArray, val width: Int, val height: Int) : ImageProcessingResult()
    data class Error(val message: String) : ImageProcessingResult()
    object Cancelled : ImageProcessingResult()
}