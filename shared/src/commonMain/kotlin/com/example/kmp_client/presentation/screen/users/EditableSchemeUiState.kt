package com.example.kmp_client.presentation.screen.users

data class EditableSchemeUiState(
    val uniqueId: String,
    var selectedSchemeId: Long?,
    var schemeName: String? = "",
    var selectedCurrentScheduleId: Long?,
    var currentScheduleName: String? = "",
    var selectedNextScheduleId: Long?,
    var nextScheduleName: String? = "",
    var isAnytime: Boolean = false,
    val isNewlyAdded: Boolean = false
)
