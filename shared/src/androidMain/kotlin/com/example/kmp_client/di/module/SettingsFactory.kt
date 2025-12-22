package com.example.kmp_client.di.module

import com.russhwolf.settings.ObservableSettings
import kotlin.UnsupportedOperationException

actual fun observableSettingsFactory(): ObservableSettings {
    throw UnsupportedOperationException(
        ""
    )
}