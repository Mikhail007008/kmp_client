/**
 * Доменная модель пользователя системы
 * Содержит всю информацию о пользователе
 */
package com.example.kmp_client.domain.model.user

import kotlinx.datetime.LocalDate

data class User(
    val id: Long,
    val code: String,
    val name: String,
    val surname: String,
    val patronymic: String,
    val jobTitleId: Long?,
    val departmentId: Long?,
    val jobTitleName: String = "",
    val departmentName: String = "",
    val features: Map<String, String> = emptyMap(),
    val guest: Boolean,
    val photo: Boolean,
    val phone: String,
    val email: String,
    val balance: Double,
    val creationTime: LocalDate?
)