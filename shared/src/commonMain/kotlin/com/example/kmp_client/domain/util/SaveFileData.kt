package com.example.kmp_client.domain.util

data class SaveFileData(
    val data: ByteArray,
    val fileName: String,
    val mimeType: String = "text/csv"
)

data class SaveFileResult(
    val success: Boolean,
    val filePath: String? = null,
    val message: String? = null,
    val error: String? = null
)

fun interface SaveFileCallback {
    fun onResult(result: SaveFileResult)
}