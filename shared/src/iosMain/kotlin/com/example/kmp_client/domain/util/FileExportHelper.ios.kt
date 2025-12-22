package com.example.kmp_client.domain.util


class FileExportHelperIOS : FileExportHelper {
    
    private var currentCallback: SaveFileCallback? = null
    private var currentData: SaveFileData? = null
    
    override suspend fun saveToFile(
        data: ByteArray,
        fileName: String,
        mimeType: String
    ): Result<Unit> {

}