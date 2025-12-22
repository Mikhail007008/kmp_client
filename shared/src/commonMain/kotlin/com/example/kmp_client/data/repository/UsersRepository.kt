/**
 * Интерфейс репозитория для работы с пользователями скуд
 * Определяет методы получения, создания, обновления, удаления пользователей и управления их данными
 */
package com.example.kmp_client.data.repository

import com.example.kmp_client.data.remote.dto.request.UserCreation
import com.example.kmp_client.data.remote.dto.request.UserUpdateRequest
import com.example.kmp_client.data.remote.dto.response.Department
import com.example.kmp_client.data.remote.dto.response.JobTitle
import com.example.kmp_client.data.remote.dto.response.ScheduleDto
import com.example.kmp_client.domain.model.user.PaginatedUsers
import com.example.kmp_client.domain.model.user.RefreshDictionariesDataSimplified
import com.example.kmp_client.domain.model.user.User
import com.example.kmp_client.domain.model.useraccess.UserAccessDetails

interface UsersRepository {
    suspend fun getUsers(
        skip: Int,
        limit: Int,
        orderBy: String? = "surname",
        filters: Map<String, String>? = null
    ): Result<PaginatedUsers>

    suspend fun getJobTitles(): Result<List<JobTitle>>
    suspend fun getDepartments(): Result<List<Department>>
    suspend fun refreshDictionaries(): Result<RefreshDictionariesDataSimplified>
    suspend fun createUser(userLoad: UserCreation): Result<Unit>
    suspend fun getUserById(userId: Long): Result<User>
    suspend fun deleteUser(userId: Long): Result<Unit>
    suspend fun updateUser(userId: Long, updateRequest: UserUpdateRequest): Result<User>
    suspend fun getUserPhoto(userId: Long): Result<ByteArray>
    suspend fun deleteUserPhoto(userId: Long): Result<Unit>
    suspend fun uploadUserPhoto(userId: Long, photoBytes: ByteArray): Result<Unit>
    suspend fun getUserAccessDetails(userId: Long): Result<UserAccessDetails>
    suspend fun updateUserAccess(userId: Long, updateAccessDetails: UserAccessDetails): Result<Unit>
    suspend fun getAllSchedules(): Result<List<ScheduleDto>>
}
