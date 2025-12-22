/**
 * ViewModel для экрана детальной информации о пользователе
 * Управляет редактированием данных пользователя, фотографией, доступом и ключами
 */
package com.example.kmp_client.presentation.screen.users

import com.example.kmp_client.core.imageUtils.ImageProcessingResult
import com.example.kmp_client.core.imageUtils.ImageSource
import com.example.kmp_client.core.imageUtils.toImageBitmap
import com.example.kmp_client.core.network.NetworkException
import com.example.kmp_client.data.remote.api.ApiConstants.PAGE_SIZE
import com.example.kmp_client.data.remote.dto.mapper.toDTO
import com.example.kmp_client.data.remote.dto.request.UserUpdateRequest
import com.example.kmp_client.data.remote.dto.response.Department
import com.example.kmp_client.data.repository.AccessSchemesRepository
import com.example.kmp_client.data.repository.KeysRepository
import com.example.kmp_client.data.repository.UsersRepository
import com.example.kmp_client.domain.model.user.User
import com.example.kmp_client.domain.model.useraccess.UserAccessDetails
import com.example.kmp_client.domain.model.useraccess.UserAccessKeyDetail
import com.example.kmp_client.domain.util.DateTimeFormatters
import com.example.kmp_client.domain.util.DateTimeFormatters.getCurrentDateForFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class UserDetailViewModel(
    private val userId: Long,
    private val usersRepository: UsersRepository,
    private val accessSchemesRepository: AccessSchemesRepository,
    private val keysRepository: KeysRepository,
    private val sharedViewModel: SharedAppViewModel,
) : ViewModel() {
    companion object {
        const val JOB_TITLE_ID_WHEN_EMPTY = 0L
        const val DEPARTMENT_ID_WHEN_EMPTY = 0L
        const val BALANCE_WHEN_EMPTY = 0.0
    }

    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    private val _jobTitlesMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val jobTitlesMap: StateFlow<Map<Long, String>> = _jobTitlesMap.asStateFlow()

    private val _departmentsMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val departmentsMap: StateFlow<Map<Long, String>> = _departmentsMap.asStateFlow()

    private var originalUserState: UserDetailUiState? = null
    private var imagePickerActionInternal: (suspend (ImageSource) -> ImageProcessingResult?)? = null

    fun setImagePickerAction(action: suspend (ImageSource) -> ImageProcessingResult?) {
        this.imagePickerActionInternal = action
    }

    init {
        loadUser()
        loadDictionaries()
    }

    fun onUploadPhotoMenuClicked() {
        _uiState.update { it.copy(showPhotoSourceDialog = true, photoUploadError = null) }
    }

    fun onDismissPhotoSourceDialog() {
        _uiState.update { it.copy(showPhotoSourceDialog = false) }
    }

    fun onPhotoSourceSelected(source: ImageSource) {
        _uiState.update { it.copy(showPhotoSourceDialog = false) }

        val currentPickerAction = imagePickerActionInternal
        if (currentPickerAction == null) {
            _uiState.update {
                it.copy(
                    isPhotoUploading = false,
                    photoUploadError = "Image picker не настроен."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPhotoLoading = true,
                    photoUploadError = null,
                    photoUploadSuccess = false
                )
            }
            try {
                val processingResult = currentPickerAction(source)

                when (processingResult) {
                    is ImageProcessingResult.Success -> {
                        usersRepository.uploadUserPhoto(userId, processingResult.bytes)
                            .onSuccess {
                                _uiState.update {
                                    it.copy(
                                        isPhotoLoading = false,
                                        isPhotoUploading = false,
                                        photoUploadError = null,
                                        photoUploadSuccess = true
                                    )
                                }
                                loadUserPhoto()
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(
                                        isPhotoLoading = false,
                                        isPhotoUploading = false,
                                        photoUploadError = error.message ?: "Ошибка загрузки фото"
                                    )
                                }
                            }
                    }

                    is ImageProcessingResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false,
                                isPhotoUploading = false,
                                photoUploadError = "Не удалось получить изображение для загрузки."
                            )
                        }
                    }

                    is ImageProcessingResult.Cancelled -> {
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false
                            )
                        }
                    }

                    null -> {
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false,
                                photoUploadError = "Не удалось получить изображение для загрузки."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPhotoLoading = false,
                        isPhotoUploading = false,
                        photoUploadError = e.message
                            ?: "Ошибка выбора фото (${e::class.simpleName})"
                    )
                }
            }
        }
    }

    private fun loadUser(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }
            usersRepository.getUserById(userId)
                .onSuccess { loadedUser ->
                    val creationDisplay = try {
                        loadedUser.creationTime?.let { date: LocalDate ->
                            date.format(DateTimeFormatters.ddMMyyFormatter)
                        } ?: ""
                    } catch (_: Exception) {
                        ""
                    }
                    val featuresFromUser = loadedUser.features
                    val keysToDisplay = featuresFromUser.keys.sorted()
                    val newState = UserDetailUiState(
                        isLoading = false,
                        user = loadedUser,
                        code = loadedUser.code,
                        name = loadedUser.name,
                        surname = loadedUser.surname,
                        patronymic = loadedUser.patronymic,
                        selectedJobTitleId = if (loadedUser.jobTitleId == 0L) null else loadedUser.jobTitleId,
                        selectedDepartmentId = if (loadedUser.departmentId == 0L) null else loadedUser.departmentId,
                        isGuest = loadedUser.guest,
                        creationTime = loadedUser.creationTime.toString(),
                        creationTimeDisplay = creationDisplay,
                        phone = loadedUser.phone,
                        email = loadedUser.email,
                        balance = if (loadedUser.balance == 0.0) "" else loadedUser.balance.toString(),
                        editableFeatures = featuresFromUser,
                        discoveredFeatureKeysOnDetail = keysToDisplay,
                        currentTab = _uiState.value.currentTab,
                        hasChanges = false
                    )
                    _uiState.value = newState
                    if (!isRefresh || originalUserState == null) {
                        originalUserState = newState.copy()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка загрузки пользователя"
                        )
                    }
                }
        }
    }

    private fun loadDictionaries(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            var currentErrorAccumulator = ""

            if (forceRefresh || _jobTitlesMap.value.isEmpty()) {

                usersRepository.getJobTitles()
                    .onSuccess { titles ->
                        _jobTitlesMap.value = titles.associate { it.id to it.name }
                    }
                    .onFailure { error ->
                        currentErrorAccumulator += "\n Ошибка загрузки должностей"
                    }
            }

            if (forceRefresh || _departmentsMap.value.isEmpty()) {
                usersRepository.getDepartments()
                    .onSuccess { deps ->
                        _departmentsMap.value =
                            withContext(Dispatchers.Default) {
                                processDepartments(deps)
                            }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                error = (it.error ?: "") + "\nОшибка загрузки подразделений"
                            )
                        }
                    }
            }

            if (forceRefresh || _uiState.value.allAccessSchemes.isEmpty()) {
                accessSchemesRepository.getAccessSchemes()
                    .onSuccess { domainSchemes ->
                        val schemesDto = domainSchemes.map { it.toDTO() }
                        val sortedSchemes =
                            schemesDto.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                        _uiState.update { it.copy(allAccessSchemes = sortedSchemes) }
                    }
                    .onFailure { error ->
                        val schemesErrorMessage = "\nОшибка загрузки схем доступа"
                        currentErrorAccumulator += schemesErrorMessage
                    }
            }

            if (forceRefresh || _uiState.value.allSchedules.isEmpty()) {
                usersRepository.getAllSchedules()
                    .onSuccess { schedules ->
                        _uiState.update { it.copy(allSchedules = schedules) }
                    }
                    .onFailure { error ->
                        val schedulesErrorMessage = "\nОшибка загрузки графиков"
                        currentErrorAccumulator += schedulesErrorMessage
                    }
            }

            _uiState.update {
                it.copy(
                    isLoadingAccessDictionaries = false,
                    accessDictionariesError = if (currentErrorAccumulator.isNotBlank()) currentErrorAccumulator.trim() else null
                )
            }
        }
    }

    private fun markAsChanged() {
        if (!_uiState.value.hasChanges) {
            _uiState.update { it.copy(hasChanges = true) }
        }
    }

    private fun processDepartments(deps: List<Department>): Map<Long, String> {
        val flatDepartmentsMap = mutableMapOf<Long, String>()
        fun flattenDepartments(departmentList: List<Department>) {
            departmentList.forEach { department ->
                flatDepartmentsMap[department.id] = department.name
                if (department.children.isNotEmpty()) {
                    flattenDepartments(department.children)
                }
            }
        }
        flattenDepartments(deps)
        return flatDepartmentsMap
    }

    fun onTabSelected(tab: UserDetailTab) {
        _uiState.update { it.copy(currentTab = tab) }

        if (tab == UserDetailTab.PHOTO && _uiState.value.userPhoto == null
            && !_uiState.value.isPhotoLoading && !_uiState.value.photoLoadAttempted
        ) {
            loadUserPhoto()
        } else if (tab == UserDetailTab.ACCESS) {
            loadUserAccessDetails()

            if (_uiState.value.allAccessSchemes.isEmpty() || _uiState.value.allSchedules.isEmpty()) {
                loadDictionaries()
            }
        }
    }

    fun loadUserAccessDetails(forceRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (!forceRefresh && currentState.userAccessDetails != null && !currentState.isAccessTabLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isAccessTabLoading = true, accessTabError = null)
            }

            val result = usersRepository.getUserAccessDetails(userId)

            _uiState.update { current ->
                result.fold(
                    onSuccess = { accessDetails ->
                        current.copy(
                            isAccessTabLoading = false,
                            userAccessDetails = accessDetails
                        )
                    },
                    onFailure = { exception ->
                        current.copy(
                            isAccessTabLoading = false,
                            accessTabError = (exception as? NetworkException)?.message
                                ?: "Ошибка загрузки доступа пользователя"
                        )
                    }
                )
            }
        }
    }

    fun onApplyAccessChanges(updatedKeyDetail: UserAccessKeyDetail) {
        val originalAccessDetails = _uiState.value.userAccessDetails
            ?: run {
                _uiState.update { it.copy(accessTabError = "Ошибка загрузки доступа пользователя") }
                return
            }

        val updatedKeysList = originalAccessDetails.keys.map { existingKey ->
            if (existingKey.key == updatedKeyDetail.key) {
                updatedKeyDetail
            } else existingKey
        }

        val newCompleteUserAccessDetails = originalAccessDetails.copy(keys = updatedKeysList)

        updateUserAccessOnServer(newCompleteUserAccessDetails)
    }

    fun onDeleteAccessKey(keyNumberToDelete: String) {
        val originalAccessDetails = _uiState.value.userAccessDetails
            ?: run {
                _uiState.update { it.copy(accessTabError = "Ошибка загрузки, отсутствуют данные для удаления") }
                return
            }

        val updatedKeysList = originalAccessDetails.keys.filterNot { it.key == keyNumberToDelete }
        val newCompleteUserAccessDetails = originalAccessDetails.copy(keys = updatedKeysList)

        updateUserAccessOnServer(newCompleteUserAccessDetails)
    }

    private fun updateUserAccessOnServer(accessDetailsToSend: UserAccessDetails) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAccessTabLoading = true, accessTabError = null) }

            val result = usersRepository.updateUserAccess(userId, accessDetailsToSend)

            result.fold(
                onSuccess = {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isAccessTabLoading = false,
                            userAccessDetails = accessDetailsToSend,
                            showEditAccessDialog = false,
                            editingAccessKey = null,
                            accessTabError = null
                        )
                    }
                    sharedViewModel.notifyUserUpdated(true, userId)
                },
                onFailure = { exception ->
                    _uiState.update { currentState ->
                        val errorMessage = (exception as? NetworkException)?.message
                            ?: "Ошибка сохранения изменений доступа"
                        currentState.copy(
                            isAccessTabLoading = false,
                            editAccessDialogError = errorMessage,
                            showEditAccessDialog = true
                        )
                    }
                }
            )
        }
    }

    fun loadUserPhoto() {
        if (_uiState.value.user == null) {
            _uiState.update {
                it.copy(
                    photoLoadingError = "Ошибка загрузки фото",
                    photoLoadAttempted = true
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPhotoLoading = true,
                    photoLoadingError = null,
                    photoLoadAttempted = true,
                    photoUploadSuccess = false
                )
            }
            usersRepository.getUserPhoto(userId)
                .onSuccess { byteArray ->
                    try {
                        val imageBitmap = withContext(Dispatchers.Default) {
                            byteArray.toImageBitmap()
                        }
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false,
                                userPhoto = imageBitmap,
                                photoLoadingError = null
                            )
                        }
                    } catch (_: Exception) {
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false,
                                userPhoto = null,
                                photoLoadingError = "Ошибка обработки фото"
                            )
                        }
                    }
                }
                .onFailure { error ->
                    if (error is NetworkException.NotFoundException) {
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false,
                                userPhoto = null,
                                photoLoadingError = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isPhotoLoading = false,
                                userPhoto = null,
                                photoLoadingError = "Ошибка загрузки фото"
                            )
                        }
                    }
                }
        }
    }

    fun onDeletePhotoClicked() {
        _uiState.update {
            it.copy(
                showDeletePhotoConfirmationDialog = true,
                photoDeleteError = null
            )
        }
    }

    fun onConfirmDeletePhoto() {
        if (_uiState.value.user == null) {
            _uiState.update {
                it.copy(
                    photoDeleteError = "Пользователь не загружен",
                    showDeletePhotoConfirmationDialog = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPhotoDeleting = true,
                    photoDeleteError = null,
                    showDeletePhotoConfirmationDialog = false
                )
            }
            usersRepository.deleteUserPhoto(userId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPhotoDeleting = false,
                            userPhoto = null,
                            photoLoadingError = null,
                            photoDeleteSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPhotoDeleting = false,
                            photoDeleteError = error.message ?: "Не удалось удалить фото"
                        )
                    }
                }
        }
    }

    fun onEditAccessKeyClicked(keyDetail: UserAccessKeyDetail) {
        if (_uiState.value.allAccessSchemes.isEmpty() || _uiState.value.allSchedules.isEmpty() &&
            !_uiState.value.isLoadingAccessDictionaries
        ) {
            loadDictionaries()
        }
        _uiState.update {
            it.copy(
                showEditAccessDialog = true,
                editingAccessKey = keyDetail,
                editAccessDialogError = null
            )
        }
    }

    fun onDismissEditAccessDialog() {
        _uiState.update {
            it.copy(
                showEditAccessDialog = false,
                editingAccessKey = null,
                editAccessDialogError = null
            )
        }
    }

    fun onDismissDeletePhotoDialog() {
        _uiState.update { it.copy(showDeletePhotoConfirmationDialog = false) }
    }

    fun onCodeChanged(newCode: String) {
        _uiState.update { it.copy(code = newCode) }; markAsChanged()
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }; markAsChanged()
    }

    fun onSurnameChanged(newSurname: String) {
        _uiState.update { it.copy(surname = newSurname) }; markAsChanged()
    }

    fun onPatronymicChanged(newPatronymic: String) {
        _uiState.update { it.copy(patronymic = newPatronymic) }; markAsChanged()
    }

    fun onFeatureValueChanged(featureKey: String, newValue: String) {
        _uiState.update { currentState ->
            val updatedEditableFeatures = currentState.editableFeatures.toMutableMap()
            updatedEditableFeatures[featureKey] = newValue
            currentState.copy(
                editableFeatures = updatedEditableFeatures,
                hasChanges = true
            )
        }
    }

    fun onPhoneChanged(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone) }; markAsChanged()
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }; markAsChanged()
    }

    fun onBalanceChanged(newBalance: String) {
        _uiState.update { it.copy(balance = newBalance) }; markAsChanged()
    }

    fun onSelectedJobTitleChanged(newJobTitleId: Long?) {
        _uiState.update { it.copy(selectedJobTitleId = newJobTitleId) }; markAsChanged()
    }

    fun onSelectedDepartmentChanged(newDepartmentId: Long?) {
        _uiState.update { it.copy(selectedDepartmentId = newDepartmentId) }; markAsChanged()
    }

    fun onApplyChangesClicked() {
        val currentState = _uiState.value
        val featuresToUpdate = currentState.editableFeatures
        val original = originalUserState ?: return

        if (!currentState.hasChanges) return

        fun <T> getFieldIfChanged(current: T, original: T): T? =
            if (current != original) current else null

        val currentJobTitleId = currentState.selectedJobTitleId ?: JOB_TITLE_ID_WHEN_EMPTY
        val originalJobTitleId = original.selectedJobTitleId ?: JOB_TITLE_ID_WHEN_EMPTY
        val currentDepartmentId = currentState.selectedDepartmentId ?: DEPARTMENT_ID_WHEN_EMPTY
        val originalDepartmentId = original.selectedDepartmentId ?: DEPARTMENT_ID_WHEN_EMPTY
        val currentBalance = currentState.balance.toDoubleOrNull() ?: BALANCE_WHEN_EMPTY
        val originalBalance = original.balance.toDoubleOrNull() ?: BALANCE_WHEN_EMPTY
        val originalFeatures = original.editableFeatures
        val featuresPayload = mutableMapOf<String, String?>()
        val allKeys = featuresToUpdate.keys + originalFeatures.keys

        for (key in allKeys) {
            val currentValue = featuresToUpdate[key]
            val originalValue = originalFeatures[key]
            if (currentValue != originalValue) {
                featuresPayload[key] = currentValue
            }
        }

        val finalFeaturesPayload = if (featuresPayload.isNotEmpty()) featuresPayload else null

        val updateRequest = UserUpdateRequest(
            code = getFieldIfChanged(currentState.code, original.code),
            name = getFieldIfChanged(currentState.name, original.name),
            surname = getFieldIfChanged(currentState.surname, original.surname),
            patronymic = getFieldIfChanged(currentState.patronymic, original.patronymic),
            jobTitleId = getFieldIfChanged(currentJobTitleId, originalJobTitleId),
            departmentId = getFieldIfChanged(currentDepartmentId, originalDepartmentId),
            features = finalFeaturesPayload,
            phone = getFieldIfChanged(currentState.phone, original.phone),
            email = getFieldIfChanged(currentState.email, original.email),
            balance = getFieldIfChanged(currentBalance, originalBalance)
        )

        val standardFieldsChanged = listOf(
            updateRequest.code,
            updateRequest.name,
            updateRequest.surname,
            updateRequest.patronymic,
            updateRequest.jobTitleId,
            updateRequest.departmentId,
            updateRequest.phone,
            updateRequest.email,
            updateRequest.balance
        ).any { it != null }

        val hasActualChanges =
            standardFieldsChanged || (updateRequest.features != null && updateRequest.features.isNotEmpty())

        if (!hasActualChanges) {
            _uiState.update { it.copy(hasChanges = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }

            usersRepository.updateUser(userId, updateRequest)
                .onSuccess { updatedUser ->
                    val newState = createUpdatedUiState(updatedUser)
                    _uiState.value = newState.copy(userPhoto = _uiState.value.userPhoto)
                    originalUserState = newState.copy(userPhoto = _uiState.value.userPhoto)
                    sharedViewModel.notifyUserUpdated(success = true, userId = userId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка сохранения изменений"
                        )
                    }
                }
        }
    }

    private fun createUpdatedUiState(updatedUser: User): UserDetailUiState {
        val selectedJobTitleId = updatedUser.jobTitleId.takeIf { it != JOB_TITLE_ID_WHEN_EMPTY }
        val selectedDepartmentId =
            updatedUser.departmentId.takeIf { it != DEPARTMENT_ID_WHEN_EMPTY }
        val balanceString =
            if (updatedUser.balance == BALANCE_WHEN_EMPTY) "" else updatedUser.balance.toString()

        val creationDisplay = try {
            updatedUser.creationTime?.let { date: LocalDate ->
                date.format(DateTimeFormatters.ddMMyyFormatter)
            } ?: ""
        } catch (_: Exception) {
            ""
        }

        return UserDetailUiState(
            isLoading = false,
            user = updatedUser,
            editableFeatures = updatedUser.features,
            discoveredFeatureKeysOnDetail = updatedUser.features.keys.sorted(),
            code = updatedUser.code,
            name = updatedUser.name,
            surname = updatedUser.surname,
            patronymic = updatedUser.patronymic,
            selectedJobTitleId = selectedJobTitleId,
            selectedDepartmentId = selectedDepartmentId,
            isGuest = updatedUser.guest,
            creationTime = updatedUser.creationTime.toString(),
            creationTimeDisplay = creationDisplay,
            phone = updatedUser.phone,
            email = updatedUser.email,
            balance = balanceString,
            currentTab = _uiState.value.currentTab,
            hasChanges = false,
            saveSuccess = true
        )
    }

    fun onDeleteUserClicked() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = true) }
    }

    fun onConfirmDeleteUser() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deleteInProgress = true,
                    showDeleteConfirmationDialog = false
                )
            }
            usersRepository.deleteUser(userId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            deleteInProgress = false,
                            wasDeletionSuccessful = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            deleteInProgress = false,
                            showDeleteResultDialog = true,
                            deleteResultMessage = error.message
                                ?: "Ошибка удаления пользователя",
                            wasDeletionSuccessful = false
                        )
                    }
                }
        }
    }

    fun onAddAccessKeyClicked() {
        _uiState.update {
            it.copy(
                showAssignKeyDialog = true,
                assignKeyDialogError = null,
                selectedKeyForAssignmentId = null,
                availableKeysForAssignment = emptyList(),
                assignKeyCurrentPage = 0,
                canLoadMoreAssignKeys = true
            )
        }
        loadAvailableKeysForAssignment(isInitialLoad = true)
    }

    fun loadAvailableKeysForAssignment(isInitialLoad: Boolean = false) {
        if (_uiState.value.isLoadingAvailableKeys || _uiState.value.isLoadingMoreAssignKeys) return
        if (!isInitialLoad && !_uiState.value.canLoadMoreAssignKeys) return

        val nextPage = if (isInitialLoad) 0 else _uiState.value.assignKeyCurrentPage + 1

        viewModelScope.launch {
            if (isInitialLoad) {
                _uiState.update {
                    it.copy(
                        isLoadingAvailableKeys = true,
                        assignKeyDialogError = null
                    )
                }
            } else _uiState.update { it.copy(isLoadingAvailableKeys = true) }

            val dateForFilter = getCurrentDateForFilter()
            val filters = mapOf(
                "filters[user][\$eq]" to "0",
                "filters[blocked][\$eq]" to "false",
                "filters[guestKey][\$eq]" to "false",
                "filters[systemKeyMode][\$eq]" to "0",
                "filters[\$or][0][expiration][\$gt]" to dateForFilter,
                "filters[\$or][1][expiration][\$eq]" to ""
            )

            val result = keysRepository.getKeys(
                skip = nextPage * PAGE_SIZE,
                limit = PAGE_SIZE,
                filters = filters,
                includeFullName = false
            )

            result.fold(
                onSuccess = { paginatedKeys ->
                    _uiState.update { currentState ->
                        val currentKeys =
                            if (isInitialLoad) emptyList() else currentState.availableKeysForAssignment
                        currentState.copy(
                            isLoadingAvailableKeys = false,
                            isLoadingMoreAssignKeys = false,
                            availableKeysForAssignment = currentKeys + paginatedKeys.items,
                            assignKeyCurrentPage = nextPage,
                            canLoadMoreAssignKeys = paginatedKeys.canLoadMore,
                            assignKeyDialogError = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoadingAvailableKeys = false,
                            isLoadingMoreAssignKeys = false,
                            assignKeyDialogError = (exception as? NetworkException)?.message
                                ?: "Ошибка загрузки ключей"
                        )
                    }
                }
            )
        }
    }

    fun onSelectKeyForAssignment(keyId: String) {
        _uiState.update { currentState ->
            if (currentState.selectedKeyForAssignmentId == keyId) {
                currentState.copy(selectedKeyForAssignmentId = null)
            } else currentState.copy(selectedKeyForAssignmentId = keyId)
        }
    }

    fun onConfirmAssignKey() {
        val selectedKeyId = _uiState.value.selectedKeyForAssignmentId ?: return
        val originalAccessDetails = _uiState.value.userAccessDetails
            ?: run {
                _uiState.update { it.copy(assignKeyDialogError = "Ошибка загрузки доступа пользователя") }
                return
            }

        val newKeyToAssign = UserAccessKeyDetail(
            key = selectedKeyId,
            controlKey = "",
            accessSchemes = emptyList()
        )

        val updatedKeysList = originalAccessDetails.keys + newKeyToAssign
        val newCompletedUserAccessDetails = originalAccessDetails.copy(keys = updatedKeysList)

        updateUserAccessOnServer(newCompletedUserAccessDetails)
        _uiState.update { it.copy(showAssignKeyDialog = false, selectedKeyForAssignmentId = null) }
    }

    fun onDismissAssignKeyDialog() {
        _uiState.update {
            it.copy(
                showAssignKeyDialog = false,
                assignKeyDialogError = null,
                selectedKeyForAssignmentId = null,
                availableKeysForAssignment = emptyList()
            )
        }
    }

    fun onCancelChangedClicked() {
        originalUserState?.let {
            _uiState.value = it.copy(currentTab = _uiState.value.currentTab)
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun clearPhotoUploadError() {
        _uiState.update { it.copy(photoUploadError = null) }
    }

    fun clearPhotoUploadSuccess() {
        _uiState.update { it.copy(photoUploadSuccess = false) }
    }

    fun clearPhotoDeletionError() {
        _uiState.update { it.copy(photoDeleteError = null) }
    }

    fun clearPhotoDeleteSuccess() {
        _uiState.update { it.copy(photoDeleteSuccess = false) }
    }

    fun clearPhotoError() {
        _uiState.update { it.copy(photoLoadingError = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearAccessTabError() {
        _uiState.update { it.copy(accessTabError = null) }
    }

    fun clearEditAccessDialogError() {
        _uiState.update { it.copy(editAccessDialogError = null) }
    }
}