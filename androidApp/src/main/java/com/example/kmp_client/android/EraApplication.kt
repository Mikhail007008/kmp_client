/**
 * Главный класс приложения Android
 * Инициализирует Koin DI, настраивает Crashlytics
 */
package com.example.kmp_client.android

import android.app.Application
import com.example.kmp_client.di.module.initKoin
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class EraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

        initKoin{
            androidLogger(Level.ERROR)
            androidContext(this@EraApplication)
        }
    }
}