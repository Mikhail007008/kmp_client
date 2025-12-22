/**
 * Общий интерфейс платформы для проекта
 * Определяет базовые функции для получения информации о платформе
 */
package com.example.kmp_client.core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun exitApp()

expect fun getAppVersion(): String