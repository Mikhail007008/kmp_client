package com.example.kmp_client.di.module

import androidx.fragment.app.FragmentActivity
import com.example.kmp_client.data.local.AndroidTokenStorage
import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.data.remote.api.CustomGetRequest
import com.example.kmp_client.domain.util.FileExportHelper
import com.example.kmp_client.domain.util.FileExportHelperAndroid
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<TokenStorage> { AndroidTokenStorage(androidContext()) }
    single<ObservableSettings> {
        SharedPreferencesSettings(
            delegate = androidContext().getSharedPreferences(
                "app_event_log_settings",
                0
            )
        )
    }

    single<CustomGetRequest> {
        CustomGetRequest(json = get())
    }

    single<FileExportHelper> {
        FileExportHelperAndroid(androidContext())
    }

    single<FragmentActivity> {
        androidContext() as FragmentActivity
    }
}
