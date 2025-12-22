/**
 * Основной DI модуль приложения
 * Конфигурирует все зависимости: репозитории, ViewModel-ы, HTTP клиенты, кеширование
 */
package com.example.kmp_client.di.module

import com.example.kmp_client.core.network.KtorClientFactory
import com.example.kmp_client.data.local.DeviceCache
import com.example.kmp_client.data.local.DeviceCacheImpl
import com.example.kmp_client.data.manager.AuthManager
import com.example.kmp_client.data.remote.api.ApiServiceImpl
import com.example.kmp_client.data.remote.api.ApiService
import com.example.kmp_client.data.repository.AccessSchemesRepository
import com.example.kmp_client.data.repository.AccessSchemesRepositoryImpl
import com.example.kmp_client.data.repository.AuthRepository
import com.example.kmp_client.data.repository.AuthRepositoryImpl
import com.example.kmp_client.data.repository.DevicesRepository
import com.example.kmp_client.data.repository.DevicesRepositoryImpl
import com.example.kmp_client.data.repository.EventsRepository
import com.example.kmp_client.data.repository.EventsRepositoryImpl
import com.example.kmp_client.data.repository.KeysRepository
import com.example.kmp_client.data.repository.KeysRepositoryImpl
import com.example.kmp_client.data.repository.UsersRepository
import com.example.kmp_client.data.repository.UsersRepositoryImpl
import com.example.kmp_client.di.DiQualifiers
import com.example.kmp_client.domain.provider.TokenProvider
import com.example.kmp_client.presentation.screen.accessscheme.AccessSchemesViewModel
import com.example.kmp_client.presentation.screen.auth.AuthViewModel
import com.example.kmp_client.presentation.screen.devices.DevicesViewModel
import com.example.kmp_client.presentation.screen.eventLog.EventLogViewModel
import com.example.kmp_client.presentation.screen.key.KeysViewModel
import com.example.kmp_client.presentation.screen.users.SharedAppViewModel
import com.example.kmp_client.presentation.screen.users.UserDetailViewModel
import com.example.kmp_client.presentation.screen.users.UsersViewModel
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun commonModule() = module {
    includes(platformModule())

    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            encodeDefaults = true
        }
    }

    single(DiQualifiers.AUTH_HTTP_CLIENT) {
        KtorClientFactory.createAuthHttpClient()
    }

    single(DiQualifiers.API_HTTP_CLIENT) {
        KtorClientFactory.createApiHttpClient (
            tokenProviderFactory = { get<TokenProvider>() }
        )
    }

    single<TokenProvider> {
        AuthManager(
            tokenStorage = get(),
            apiService = get()
        )
    }

    single<ApiService> {
        ApiServiceImpl(
            authHttpClient = get(DiQualifiers.AUTH_HTTP_CLIENT),
            apiHttpClient = get(DiQualifiers.API_HTTP_CLIENT),
            tokenStorage = get(),
            customGetRequest = get(),
            json = get()
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            apiService = get(),
            tokenStorage = get()
        )
    }

    single<DevicesRepository> {
        DevicesRepositoryImpl(
            apiService = get(),
            tokenStorage = get()
        )
    }

    single<AccessSchemesRepository> {
        AccessSchemesRepositoryImpl(
            apiService = get(),
            tokenStorage = get()
        )
    }

    single<UsersRepository> {
        UsersRepositoryImpl(
            apiService = get(),
            tokenStorage = get(),
            json = get()
        )
    }

    single<KeysRepository> {
        KeysRepositoryImpl(
            apiService = get(),
            tokenStorage = get(),
            usersRepository = get(),
            accessSchemesRepository = get(),
            json = get()
        )
    }

    single<DeviceCache> {
        DeviceCacheImpl(
            settings = get(),
            json = get()
        )
    }

    single<EventsRepository> {
        EventsRepositoryImpl(
            apiService = get(),
            tokenStorage = get(),
            deviceCache = get()
        )
    }

    factory { (userId: Long, sharedViewModel: SharedAppViewModel) ->
        UserDetailViewModel(
            userId = userId,
            usersRepository = get(),
            accessSchemesRepository = get(),
            keysRepository = get(),
            sharedViewModel = sharedViewModel
        )
    }

    factory { DevicesViewModel(devicesRepository = get(), deviceCache = get()) }

    singleOf(::AuthViewModel)
    singleOf(::SharedAppViewModel)
    factoryOf(::AccessSchemesViewModel)
    factoryOf(::UsersViewModel)
    factoryOf(::KeysViewModel)
    factoryOf(::EventLogViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule(), platformModule())
}