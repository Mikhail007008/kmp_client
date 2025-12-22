/**
 * Android реализация TokenStorage для хранения токенов аутентификации
 * Сохраняет токены в SharedPreferences для работы на Android устройствах
 */
package com.example.kmp_client.data.local

import android.content.Context
import com.example.kmp_client.data.local.storage.TokenStorage

private const val KEY_LAST_SERVER_URL = "last_server_url"
private const val KEY_LAST_PORT = "last_port"

class AndroidTokenStorage(context: Context) : TokenStorage {
}