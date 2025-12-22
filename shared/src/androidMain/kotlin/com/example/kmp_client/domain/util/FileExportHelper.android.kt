/**
 * Android реализация FileExportHelper для экспорта файлов в приложении
 * Поддерживает сохранение в папку загрузок и через диалог сохранения
 */
package com.example.kmp_client.domain.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileOutputStream

class FileExportHelperAndroid(
    private val context: Context
) : FileExportHelper {

    private var currentCallback: SaveFileCallback? = null
    private var currentSaveData: SaveFileData? = null
    private var saveFileLauncher: ActivityResultLauncher<Intent>? = null
    private var activity: FragmentActivity? = null

    fun setActivity(activity: FragmentActivity) {
        this.activity = activity
        saveFileLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSaveFileResult(result.data)
        }
    }
    
    override suspend fun saveToFile(
        data: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<Unit> {
        return try {
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val file = File(downloadsDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data)
            }
            
            scanFileForMediaStore(file)
            Toast.makeText(
                context,
                "Файл сохранен: ${file.name}",
                Toast.LENGTH_SHORT
            ).show()
            
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(Exception("Ошибка при сохранении файла"))
        }
    }
    
    override fun showSaveDialog(saveData: SaveFileData, callback: SaveFileCallback) {
        currentCallback = callback
        currentSaveData = saveData
        
        if (activity != null && saveFileLauncher != null) {
            showSafSaveDialog(saveData, callback)
        } else {
            saveToStandardLocation(saveData, callback)
        }
    }

    private fun showSafSaveDialog(saveData: SaveFileData, callback: SaveFileCallback) {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType(saveData.mimeType)
                putExtra(Intent.EXTRA_TITLE, saveData.fileName)
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(saveData.mimeType))
            }
            
            currentCallback = callback
            saveFileLauncher?.launch(intent) ?: run {
                callback.onResult(SaveFileResult(
                    success = false,
                    error = "Не удалось запустить диалог сохранения"
                ))
            }
            
        } catch (_: Exception) {
            callback.onResult(SaveFileResult(
                success = false,
                error = "Ошибка при открытии диалога"
            ))
        }
    }

    private fun handleSaveFileResult(data: Intent?) {
        val callback = currentCallback ?: return
        val saveData = currentSaveData ?: run {
            callback.onResult(SaveFileResult(
                success = false,
                error = "Данные для сохранения не найдены"
            ))
            return
        }
        
        try {
            val uri = data?.data
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(saveData.data)
                }
                
                callback.onResult(SaveFileResult(
                    success = true,
                    message = "Файл сохранен в выбранном месте: ${saveData.fileName}"
                ))
            } else {
                callback.onResult(SaveFileResult(
                    success = false,
                    error = "Место сохранения не выбрано"
                ))
            }
        } catch (_: Exception) {
            callback.onResult(SaveFileResult(
                success = false,
                error = "Ошибка при сохранении"
            ))
        } finally {
            currentCallback = null
            currentSaveData = null
        }
    }

    private fun saveToStandardLocation(saveData: SaveFileData, callback: SaveFileCallback) {
        try {
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val file = File(downloadsDir, saveData.fileName)
            
            FileOutputStream(file).use { outputStream ->
                outputStream.write(saveData.data)
            }
            
            scanFileForMediaStore(file)
            callback.onResult(SaveFileResult(
                success = true,
                filePath = file.absolutePath,
                message = "Файл сохранен в папку загрузок: ${file.absolutePath}"
            ))
            
        } catch (_: Exception) {
            callback.onResult(SaveFileResult(
                success = false,
                error = "Ошибка при сохранении"
            ))
        }
    }

    private fun scanFileForMediaStore(file: File) {
        try {
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = uri
            }
            context.sendBroadcast(intent)
        } catch (_: Exception) {
        }
    }
}