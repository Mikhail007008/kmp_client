/**
 * Главная активность Android приложения
 * Управляет UI, разрешениями, back button логикой и инициализацией FileExportHelper
 */
package com.example.kmp_client.android

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.example.kmp_client.di.App
import org.koin.java.KoinJavaComponent.getKoin
import com.example.kmp_client.domain.util.FileExportHelper
import com.example.kmp_client.domain.util.FileExportHelperAndroid

class MainActivity : FragmentActivity() {
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Разрешение на использование камеры необходимо для работы приложения", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fileExportHelper = getKoin().get<FileExportHelper>()
        if (fileExportHelper is FileExportHelperAndroid) {
            fileExportHelper.setActivity(this)
        }

        setContent {
            App()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            private var backPressedTime: Long = 0

            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000) {
                    moveTaskToBack(true)
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this@MainActivity, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}