/**
 * UI экран для управления фотографией пользователя
 * Показывает текущее фото, позволяет загрузить новое из камеры/галереи или удалить
 */
package com.example.kmp_client.presentation.screen.users

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kmp_client.core.imageUtils.ImageSource
import org.jetbrains.compose.resources.stringResource
import kmp_eraclient.shared.generated.resources.*

@Composable
fun UserPhotoTabScreen(
    uiState: UserDetailUiState,
    viewModel: UserDetailViewModel
) {
    Box(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        when {
            uiState.isPhotoLoading || uiState.isPhotoUploading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.photoLoadingError != null -> {
                PhotoErrorState(
                    errorMessage = "Ошибка загрузки фото",
                    onRetry = {
                        viewModel.clearPhotoError()
                        viewModel.loadUserPhoto()
                    }
                )
            }

            uiState.photoUploadError != null -> {
                PhotoErrorState(
                    errorMessage = "Ошибка загрузки фото",
                    onRetry = {
                        viewModel.clearPhotoUploadError()
                    }
                )
            }

            uiState.userPhoto != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = uiState.userPhoto,
                        contentDescription = "Фото пользователя ${uiState.user?.name ?: ""}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            15.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        Button(
                            onClick = { viewModel.onUploadPhotoMenuClicked() },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            enabled = !uiState.isPhotoDeleting && !uiState.isPhotoUploading
                        ) {
                            Text(stringResource(Res.string.change))
                        }
                        Button(
                            onClick = { viewModel.onDeletePhotoClicked() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            enabled = !uiState.isPhotoDeleting && !uiState.isPhotoUploading
                        ) {
                            if (uiState.isPhotoDeleting) {
                                Text(stringResource(Res.string.deleting))
                            } else {
                                Text(stringResource(Res.string.delete_photo_button))
                            }
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(Res.string.user_no_photo),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(
                        onClick = { viewModel.onUploadPhotoMenuClicked() }
                    ) {
                        Text(stringResource(Res.string.upload_photo))
                    }
                }
            }
        }

        if (uiState.showPhotoSourceDialog) {
            PhotoSourceSelectionDialog(
                onDismissRequest = { viewModel.onDismissPhotoSourceDialog() },
                onSourceSelected = { source ->
                    viewModel.onPhotoSourceSelected(source)
                }
            )
        }

        if (uiState.showDeletePhotoConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeletePhotoDialog() },
                title = { Text(stringResource(Res.string.delete_photo)) },
                text = { Text(stringResource(Res.string.delete_photo_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.onConfirmDeletePhoto() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(Res.string.delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDeletePhotoDialog() }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun PhotoSourceSelectionDialog(
    onDismissRequest: () -> Unit,
    onSourceSelected: (ImageSource) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.chose_photo)) },
        text = {
            Column {
                TextButton(
                    onClick = {
                        onSourceSelected(ImageSource.GALLERY)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(stringResource(Res.string.gallery))
                }
                HorizontalDivider()
                TextButton(
                    onClick = {
                        onSourceSelected(ImageSource.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(stringResource(Res.string.camera))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun BoxScope.PhotoErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
    }
}