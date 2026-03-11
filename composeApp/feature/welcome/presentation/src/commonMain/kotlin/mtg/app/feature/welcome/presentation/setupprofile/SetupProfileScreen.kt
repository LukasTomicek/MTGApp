package mtg.app.feature.welcome.presentation.setupprofile

import mtg.app.core.presentation.state.UiState
import mtg.app.feature.welcome.presentation.utils.WelcomeNavButtons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import mtg.app.core.presentation.components.appOutlinedTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SetupProfileScreen(
    uiState: UiState<SetupProfileScreenState>,
    onUiEvent: (SetupProfileUiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp)
                .padding(bottom = 110.dp),
        ) {
            Text(
                modifier = Modifier.padding(bottom = 24.dp),
                text = uiState.data.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                modifier = Modifier.padding(bottom = 12.dp),
                text = uiState.data.subtitle,
                style = MaterialTheme.typography.bodyLarge,
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                value = uiState.data.nickname,
                onValueChange = { onUiEvent(SetupProfileUiEvent.NicknameChanged(it)) },
                label = { Text("Nickname") },
                singleLine = true,
                colors = appOutlinedTextFieldColors(),
            )

            uiState.data.nicknameError?.takeIf { it.isNotBlank() }?.let { error ->
                Text(
                    modifier = Modifier.padding(bottom = 12.dp),
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        WelcomeNavButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .padding(vertical = 16.dp),
            isBackHidden = false,
            continueLabel = "Finish",
            continueEnabled = uiState.data.nickname.trim().isNotBlank() && !uiState.data.isSavingNickname,
            onBackClicked = { onUiEvent(SetupProfileUiEvent.BackClicked) },
            onContinueClicked = { onUiEvent(SetupProfileUiEvent.ContinueClicked) },
        )
    }
}
