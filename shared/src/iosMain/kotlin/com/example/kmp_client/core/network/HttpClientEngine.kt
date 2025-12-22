package com.example.kmp_client.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpClient(): HttpClientEngine {
    return Darwin.create { }
}