package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Long,
    viewModel: UserDetailViewModel,
    sharedViewModel: SharedAppViewModel,
    navController: Navigator,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val jobTitlesMap by viewModel.jobTitlesMap.collectAsState()
    val departmentsMap by viewModel.departmentsMap.collectAsState()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Изменения успешно сохранены!")
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.photoDeleteSuccess) {
        if (uiState.photoDeleteSuccess) {
            snackbarHostState.showSnackbar("Фотография успешно удалена")
            viewModel.clearPhotoDeleteSuccess()
        }
    }


    LaunchedEffect(uiState.photoDeleteError) {
        uiState.photoDeleteError?.let { error ->
            snackbarHostState.showSnackbar("Ошибка удаления фото: $error")
            viewModel.clearPhotoDeletionError()
        }
    }

    LaunchedEffect(uiState.wasDeletionSuccessful) {
        if (uiState.wasDeletionSuccessful && !uiState.deleteInProgress) {
            sharedViewModel.notifyUserDeleted(true)
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.photoUploadSuccess) {
        if (uiState.photoUploadSuccess) {
            snackbarHostState.showSnackbar("Фотография успешно загружена")
            viewModel.clearPhotoUploadSuccess()
        }
    }

    LaunchedEffect(uiState.accessTabError) {
        uiState.accessTabError?.let {
            snackbarHostState.showSnackbar("Доступ: $it")
            viewModel.clearAccessTabError()
        }
    }

    Scaffold(
        bottomBar = {
            UserDetailBottomAppBar(
                currentTab = uiState.currentTab,
                onTabSelected = { selectedTab ->
                    viewModel.onTabSelected(selectedTab)
                },
                onBackClicked = {
                    if (uiState.showDeleteConfirmationDialog || uiState.showDeletePhotoConfirmationDialog) {
                        return@UserDetailBottomAppBar
                    }

                    if (uiState.currentTab != UserDetailTab.PROPERTIES) {
                        viewModel.onTabSelected(UserDetailTab.PROPERTIES)
                    } else navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading || uiState.deleteInProgress) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.user != null) {
                when (uiState.currentTab) {
                    UserDetailTab.PROPERTIES -> UserPropertiesTabScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        jobTitlesMap = jobTitlesMap,
                        departmentsMap = departmentsMap
                    )

                    UserDetailTab.PHOTO -> UserPhotoTabScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    UserDetailTab.ACCESS -> UserAccessTabScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        userId = userId,
                        snackbarHostState = snackbarHostState
                    )
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.error_user_details),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserDetailBottomAppBar(
    currentTab: UserDetailTab,
    onTabSelected: (UserDetailTab) -> Unit,
    onBackClicked: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab == UserDetailTab.PROPERTIES,
            onClick = { onTabSelected(UserDetailTab.PROPERTIES) },
            icon = { Icon(Icons.Filled.Info, contentDescription = "Свойства") },
            label = { Text(stringResource(Res.string.properties)) }
        )
        NavigationBarItem(
            selected = currentTab == UserDetailTab.PHOTO,
            onClick = { onTabSelected(UserDetailTab.PHOTO) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Фото") },
            label = { Text(stringResource(Res.string.photo)) }
        )
        NavigationBarItem(
            selected = currentTab == UserDetailTab.ACCESS,
            onClick = { onTabSelected(UserDetailTab.ACCESS) },
            icon = { Icon(Icons.Filled.Lock, contentDescription = "Доступ") },
            label = { Text(stringResource(Res.string.access)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { onBackClicked() },
            icon = { Icon(Filled.ArrowBack, contentDescription = "Назад") },
            label = { Text(stringResource(Res.string.back)) }
        )
    }
}