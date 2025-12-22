package com.example.kmp_client.di

import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

object DiQualifiers {
    val AUTH_HTTP_CLIENT: StringQualifier = named("AuthHttpClient")
    val API_HTTP_CLIENT: StringQualifier = named("ApiHttpClient")
}