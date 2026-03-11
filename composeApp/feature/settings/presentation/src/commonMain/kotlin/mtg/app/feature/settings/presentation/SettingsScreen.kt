package mtg.app.feature.settings.presentation

import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    uiState: UiState<SettingsScreenState>,
    onUiEvent: (SettingsUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = uiState.data.subtitle,
            style = MaterialTheme.typography.bodyLarge,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                state = AppButtonState(
                    title = TextState("Profile"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(SettingsUiEvent.ProfileClicked) },
                ),
            )
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                state = AppButtonState(
                    title = TextState("Run onboarding"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(SettingsUiEvent.OnboardingClicked) },
                ),
            )
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                state = AppButtonState(
                    title = TextState("Privacy policy"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(SettingsUiEvent.PrivacyPolicyClicked) },
                ),
            )
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                state = AppButtonState(
                    title = TextState("Terms of use"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(SettingsUiEvent.TermsOfUseClicked) },
                ),
            )
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                state = AppButtonState(
                    title = TextState("Logout"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(SettingsUiEvent.LogoutClicked) },
                ),
            )
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                state = AppButtonState(
                    title = TextState("Delete account"),
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(SettingsUiEvent.DeleteAccountClicked) },
                ),
            )
        }
        uiState.error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (uiState.data.actionMessage.isNotBlank()) {
            Text(
                text = uiState.data.actionMessage,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
