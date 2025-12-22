package com.example.kmp_client.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideHttpClient(): HttpClientEngine {
    return OkHttp.create{}
}