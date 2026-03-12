package mtg.app.feature.settings.presentation.profile

import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.AppValueEditModal
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    uiState: UiState<ProfileScreenState>,
    onUiEvent: (ProfileUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = "Nickname: ${uiState.data.nickname.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        uiState.data.nicknameError?.takeIf { it.isNotBlank() }?.let { error ->
            item {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (uiState.data.infoMessage.isNotBlank()) {
            item {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = uiState.data.infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item {
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                state = AppButtonState(
                    title = TextState("Change nickname"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(ProfileUiEvent.ChangeNicknameClicked) },
                ),
            )
        }

        item {
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                state = AppButtonState(
                    title = TextState("Change password"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(ProfileUiEvent.ChangePasswordClicked) },
                ),
            )
        }

        item {
            Text(
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                text = "My reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        uiState.data.reviewsError?.takeIf { it.isNotBlank() }?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (uiState.data.reviews.isEmpty()) {
            item {
                Text(
                    text = if (uiState.isLoading) "Loading reviews..." else "No reviews yet",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(uiState.data.reviews, key = { "${it.createdAt}_${it.score}_${it.comment.hashCode()}" }) { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Score: ${review.score}/5",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        if (review.comment.isNotBlank()) {
                            Text(
                                text = review.comment,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.data.isChangeNicknameModalVisible) {
        AppValueEditModal(
            currentValue = "",
            newPassword = uiState.data.nicknameDraftInput,
            confirmPassword = "",
            onCurrentValueChanged = { },
            onNewPasswordChanged = { onUiEvent(ProfileUiEvent.NicknameDraftChanged(it)) },
            onConfirmPasswordChanged = { },
            onDismiss = { onUiEvent(ProfileUiEvent.ChangeNicknameDismissed) },
            onSubmit = { onUiEvent(ProfileUiEvent.ChangeNicknameConfirmed) },
            showCurrentValue = false,
            showConfirmPassword = false,
            errorMessage = uiState.data.nicknameError,
            isSaving = uiState.isLoading,
            title = "Change nickname",
            primaryLabel = "Nickname",
            primaryIsSecret = false,
        )
    }

    if (uiState.data.isChangePasswordModalVisible) {
        AppValueEditModal(
            currentValue = uiState.data.currentPasswordInput,
            newPassword = uiState.data.newPasswordInput,
            confirmPassword = uiState.data.confirmPasswordInput,
            onCurrentValueChanged = { onUiEvent(ProfileUiEvent.CurrentPasswordChanged(it)) },
            onNewPasswordChanged = { onUiEvent(ProfileUiEvent.NewPasswordChanged(it)) },
            onConfirmPasswordChanged = { onUiEvent(ProfileUiEvent.ConfirmPasswordChanged(it)) },
            onDismiss = { onUiEvent(ProfileUiEvent.ChangePasswordDismissed) },
            onSubmit = { onUiEvent(ProfileUiEvent.ChangePasswordConfirmed) },
            showCurrentValue = true,
            showConfirmPassword = true,
            errorMessage = uiState.data.passwordError,
            isSaving = uiState.isLoading,
        )
    }
}
