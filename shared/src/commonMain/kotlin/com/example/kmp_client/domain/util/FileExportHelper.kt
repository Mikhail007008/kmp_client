/**
 * Интерфейс для экспорта файлов в приложении
 * Определяет методы сохранения данных в файлы и генерации имен файлов
 */
package com.example.kmp_client.domain.util

import kotlinx.datetime.Clock.System

interface FileExportHelper {
    suspend fun saveToFile(
        data: ByteArray,
        fileName: String,
        mimeType: String = "text/csv"
    ): Result<Unit>

    fun showSaveDialog(saveData: SaveFileData, callback: SaveFileCallback)

    fun generateFileName(prefix: String = "event_log", extension: String = "csv"): String {
        val timestamp = System.now()
        return "${prefix}_${timestamp}.${extension}"
    }
}