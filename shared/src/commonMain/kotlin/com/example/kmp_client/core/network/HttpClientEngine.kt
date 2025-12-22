package com.example.kmp_client.core.network

import io.ktor.client.engine.HttpClientEngine

expect fun provideHttpClient(): HttpClientEngine