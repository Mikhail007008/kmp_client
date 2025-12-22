/**
 * Общий ViewModel для событий пользователей в приложении
 * Управляет уведомлениями об удалении и обновлении пользователей между экранами
 */
package com.example.kmp_client.presentation.screen.users

import com.example.kmp_client.domain.model.user.UserDeletionEvent
import com.example.kmp_client.domain.model.user.UserUpdateEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import moe.tlaster.precompose.viewmodel.ViewModel

class SharedAppViewModel : ViewModel() {
    private val _userDeletedEvent = MutableStateFlow<UserDeletionEvent?>(null)
    val userDeletedEvent: StateFlow<UserDeletionEvent?> = _userDeletedEvent.asStateFlow()

    private val _userUpdatedEvent = MutableStateFlow<UserUpdateEvent?>(null)
    val userUpdatedEvent: StateFlow<UserUpdateEvent?> = _userUpdatedEvent.asStateFlow()

    fun notifyUserDeleted(success: Boolean) {
        _userDeletedEvent.value = UserDeletionEvent(success)
    }

    fun clearUserDeletedEvent() {
        _userDeletedEvent.value = null
    }

    fun notifyUserUpdated(success: Boolean, userId: Long) {
        _userUpdatedEvent.value = UserUpdateEvent(success, userId)
    }

    fun clearUserUpdatedEvent() {
        _userUpdatedEvent.value = null
    }
}