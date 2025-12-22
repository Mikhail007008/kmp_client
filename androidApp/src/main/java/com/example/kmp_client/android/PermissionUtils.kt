/**
 * Управление разрешениями Android
 * Поддерживает разные версии Android
 */
package com.example.kmp_client.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionManager {

    fun isCameraAllowed(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isStorageAllowed(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPerms(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            @Suppress("DEPRECATION")
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun checkAllPermissions(context: Context): Boolean {
        return isCameraAllowed(context) && isStorageAllowed(context)
    }

    fun getUserFriendlyMessage(context: Context, perm: String): String {
        return when (perm) {
            Manifest.permission.CAMERA ->
                "Нужно разрешение на камеру для съемки фото профиля"
            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE ->
                "Нужно разрешение на доступ к галерее для выбора фото"
            else -> "Требуется разрешение: $perm"
        }
    }

    fun checkPermissionWithLog(context: Context, permission: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        android.util.Log.d("PermissionManager", "Permission $permission: ${if (granted) "GRANTED" else "DENIED"}")
        return granted
    }
}