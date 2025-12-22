package com.example.kmp_client.di.module

import com.example.kmp_client.data.local.iOSTokenStorage
import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.domain.util.FileExportHelper
import com.example.kmp_client.domain.util.FileExportHelperIOS
import com.russhwolf.settings.ObservableSettings
import org.koin.core.module.Module
import org.koin.dsl.module


actual fun platformModule(): Module  = module{
    single<TokenStorage> { iOSTokenStorage() }
    single<ObservableSettings> { observableSettingsFactory() }

    single<FileExportHelper> {
        FileExportHelperIOS()
    }
}