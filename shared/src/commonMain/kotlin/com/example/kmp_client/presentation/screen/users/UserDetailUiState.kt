package com.example.kmp_client.presentation.screen.users

import androidx.compose.ui.graphics.ImageBitmap
import com.example.kmp_client.data.remote.dto.response.AccessSchemeDTO
import com.example.kmp_client.data.remote.dto.response.ScheduleDto
import com.example.kmp_client.domain.model.key.Key
import com.example.kmp_client.domain.model.user.User
import com.example.kmp_client.domain.model.useraccess.UserAccessDetails
import com.example.kmp_client.domain.model.useraccess.UserAccessKeyDetail

enum class UserDetailTab {
    PROPERTIES, PHOTO, ACCESS
}

data class UserDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val editableFeatures: Map<String, String> = emptyMap(),
    val discoveredFeatureKeysOnDetail: List<String> = emptyList(),
    val error: String? = null,
    val currentTab: UserDetailTab = UserDetailTab.PROPERTIES,
    val code: String = "",
    val name: String = "",
    val surname: String = "",
    val patronymic: String = "",
    val selectedJobTitleId: Long? = null,
    val selectedDepartmentId: Long? = null,
    val isGuest: Boolean = false,
    val phone: String = "",
    val email: String = "",
    val balance: String = "0.0",
    val creationTime: String = "",
    val creationTimeDisplay: String = "",
    val saveSuccess: Boolean = false,
    val deleteConfirmationDialog: Boolean = false,
    val hasChanges: Boolean = false,
    val showDeleteConfirmationDialog: Boolean = false,
    val deleteInProgress: Boolean = false,
    val deleteError: String? = null,
    val showDeleteResultDialog: Boolean = false,
    val deleteResultMessage: String? = null,
    val wasDeletionSuccessful: Boolean = false,
    val userPhoto: ImageBitmap? = null,
    val isPhotoLoading: Boolean = false,
    val photoLoadingError: String? = null,
    val isPhotoDeleting: Boolean = false,
    val photoDeleteError: String? = null,
    val showDeletePhotoConfirmationDialog: Boolean = false,
    val photoDeleteSuccess: Boolean = false,
    val photoLoadAttempted: Boolean = false,
    val isPhotoUploading: Boolean = false,
    val photoUploadError: String? = null,
    val showPhotoSourceDialog: Boolean = false,
    val photoUploadSuccess: Boolean = false,
    val isAccessTabLoading: Boolean = false,
    val userAccessDetails: UserAccessDetails? = null,
    val accessTabError: String? = null,
    val showEditAccessDialog: Boolean = false,
    val editingAccessKey: UserAccessKeyDetail? = null,
    val allAccessSchemes: List<AccessSchemeDTO> = emptyList(),
    val allSchedules: List<ScheduleDto> = emptyList(),
    val isLoadingAccessDictionaries: Boolean = false,
    val accessDictionariesError: String? = null,
    val editAccessDialogError: String? = null,
    val showAssignKeyDialog: Boolean = false,
    val availableKeysForAssignment: List<Key> = emptyList(),
    val isLoadingAvailableKeys: Boolean = false,
    val assignKeyDialogError: String? = null,
    val selectedKeyForAssignmentId: String? = null,
    val assignKeyCurrentPage: Int = 0,
    val canLoadMoreAssignKeys: Boolean = true,
    val isLoadingMoreAssignKeys: Boolean = false,
)