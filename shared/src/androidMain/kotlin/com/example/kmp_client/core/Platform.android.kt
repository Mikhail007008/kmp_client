/**
 * Android реализация интерфейса Platform для KMP проекта
 * Предоставляет информацию об Android устройстве и его версии
 */
package com.example.kmp_client.core

import android.content.Intent
import android.os.Build
import org.koin.java.KoinJavaComponent.getKoin

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun exitApp() {
    val context = getKoin().get<android.content.Context>()
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

actual fun getAppVersion(): String {
    val context = getKoin().get<android.content.Context>()
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: ""
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}