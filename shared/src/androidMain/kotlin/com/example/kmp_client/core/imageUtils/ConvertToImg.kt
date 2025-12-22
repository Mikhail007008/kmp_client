package com.example.kmp_client.core.imageUtils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    if (bitmap != null) {
        return bitmap.asImageBitmap()
    } else {
        throw IllegalArgumentException("Не валидное изображение")
    }
}