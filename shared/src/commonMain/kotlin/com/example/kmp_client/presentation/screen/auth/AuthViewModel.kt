/**
 * ViewModel для экрана аутентификации
 * Управляет логином, автологином, валидацией данных и состоянием UI
 */
package com.example.kmp_client.presentation.screen.auth

import com.example.kmp_client.data.local.storage.TokenStorage
import com.example.kmp_client.core.network.NetworkException
import com.example.kmp_client.data.remote.dto.request.AuthRequest
import com.example.kmp_client.data.repository.AuthRepository
import com.example.kmp_client.core.secutiry.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUIState(isLoading = true))
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            val savedUrl = tokenStorage.getLastServerUrl() ?: ""
            val savedPort = tokenStorage.getLastPort() ?: ""
            val autoAuthEnabled = tokenStorage.getAutoAuthEnabled()
            val savedUsername = tokenStorage.getLastUsername()
            val savedHashedPassword = tokenStorage.getLastHashedPassword()

            _uiState.update {
                it.copy(
                    port = savedPort,
                    serverUrl = savedUrl,
                    autoAuthEnabled = autoAuthEnabled,
                    isLoading = false
                )
            }

            if (autoAuthEnabled && savedUsername != null && savedHashedPassword != null) {
                _uiState.update {
                    it.copy(
                        username = savedUsername,
                        password = "",
                        isAutoAuthMode = true,
                        countdown = 2
                    )
                }
                startCountdown()
            }
        }
    }

    fun onServerUrlChanged(newUrl: String) {
        _uiState.update { it.copy(serverUrl = newUrl.trim()) }
    }

    fun onPortChanged(newPort: String) {
        if (newPort.all { it.isDigit() } || newPort.isEmpty()) {
            _uiState.update { it.copy(port = newPort.trim()) }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername.trim()) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onAutoAuthChanged(enabled: Boolean) {
        val currentState = _uiState.value
        _uiState.update { it.copy(autoAuthEnabled = enabled) }
        viewModelScope.launch {
            tokenStorage.saveAutoAuthEnabled(enabled)
        }
        if (!enabled && currentState.isAutoAuthMode) {
            countdownJob?.cancel()
            clearAutoAuthData()
        }
    }

    fun login() {
        if (_uiState.value.isLoading) return

        val currentState = _uiState.value
        if (currentState.serverUrl.isBlank() || currentState.port.isBlank()
            || currentState.username.isBlank() || currentState.password.isBlank()
        ) {
            _uiState.update { it.copy(error = "Все поля обязательны для заполнения") }
            return
        }

        val portNumber = currentState.port.toIntOrNull()
        if (portNumber == null || portNumber <= 0 || portNumber > 65535) {
            _uiState.update { it.copy(error = "Некорректный порт") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        val hashedPassword = HashUtil.md5(currentState.password)
        val authRequestDto = AuthRequest(
            user = currentState.username,
            password = hashedPassword
        )

        viewModelScope.launch(Dispatchers.Main) {
            try {
                val result = withContext(Dispatchers.IO) {
                    authRepository.login(
                        serverUrl = currentState.serverUrl,
                        port = currentState.port,
                        authRequest = authRequestDto
                    )
                }

                result.fold(
                    onSuccess = { sessionData ->
                        if (currentState.autoAuthEnabled) {
                            viewModelScope.launch {
                                tokenStorage.saveLastUserCredentials(
                                    username = currentState.username,
                                    hashedPassword = hashedPassword,
                                    serverUrl = currentState.serverUrl,
                                    port = currentState.port
                                )
                                tokenStorage.saveAutoAuthEnabled(true)
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = when (exception) {
                            is NetworkException.TimeoutException -> "Превышено время ожидания ответа от сервера."
                            is NetworkException.ConnectionException -> "Не удалось подключиться к серверу. Проверьте адрес, порт и интернет-соединение."
                            is NetworkException.ServerException -> "Ошибка сервера (${exception.statusCode})"
                            else -> "Ошибка авторизации"
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage,
                                isAuthenticated = false
                            )
                        }
                    }
                )
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка авторизации",
                        isAuthenticated = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun clearAutoAuthData() {
        viewModelScope.launch {
            tokenStorage.clearAllAuthData()
            _uiState.update {
                it.copy(
                    username = "",
                    password = "",
                    isAutoAuthMode = false,
                    countdown = 0,
                    autoAuthEnabled = false
                )
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var countdown = 3
            while (countdown > 0) {
                _uiState.update { it.copy(countdown = countdown) }
                delay(1000)
                countdown--
            }
            _uiState.update { it.copy(countdown = 0) }
            performAutoLogin()
        }
    }

    private suspend fun performAutoLogin() {
        val currentState = _uiState.value
        if (currentState.isAutoAuthMode) {
            val savedUsername = tokenStorage.getLastUsername()
            val savedHashedPassword = tokenStorage.getLastHashedPassword()
            val savedUrl = tokenStorage.getLastServerUrl()
            val savedPort = tokenStorage.getLastPort()

            if (savedUsername != null && savedHashedPassword != null && savedUrl != null && savedPort != null) {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val authRequestDto = AuthRequest(
                    user = savedUsername,
                    password = savedHashedPassword
                )

                viewModelScope.launch(Dispatchers.Main) {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            authRepository.login(
                                serverUrl = savedUrl,
                                port = savedPort,
                                authRequest = authRequestDto
                            )
                        }

                        result.fold(
                            onSuccess = { sessionData ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        error = null
                                    )
                                }
                            },
                            onFailure = { exception ->
                                val errorMessage = when (exception) {
                                    is NetworkException.TimeoutException -> "Превышено время ожидания ответа от сервера."
                                    is NetworkException.ConnectionException -> "Не удалось подключиться к серверу. Проверьте адрес, порт и интернет-соединение."
                                    is NetworkException.ServerException -> "Ошибка сервера (${exception.statusCode})"
                                    else -> "Ошибка авторизации"
                                }

                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = errorMessage,
                                        isAuthenticated = false,
                                        isAutoAuthMode = false,
                                        countdown = 0
                                    )
                                }
                            }
                        )
                    } catch (_: Exception) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Ошибка авторизации",
                                isAuthenticated = false,
                                isAutoAuthMode = false,
                                countdown = 0
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAutoAuthMode = false,
                        countdown = 0
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}