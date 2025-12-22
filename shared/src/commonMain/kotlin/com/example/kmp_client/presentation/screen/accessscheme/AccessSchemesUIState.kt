package com.example.kmp_client.presentation.screen.accessscheme

import com.example.kmp_client.domain.model.accesssheme.AccessScheme

data class AccessSchemesUIState(
    val isLoading: Boolean = false,
    val accessSchemes: List<AccessScheme> = emptyList(),
    val error: String? = null
)
