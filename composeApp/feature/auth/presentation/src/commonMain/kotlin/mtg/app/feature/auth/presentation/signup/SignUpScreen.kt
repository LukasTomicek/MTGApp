package mtg.app.feature.auth.presentation.signup

import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.components.appOutlinedTextFieldColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    uiState: UiState<SignUpScreenState>,
    onUiEvent: (SignUpUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Create account",
            style = MaterialTheme.typography.headlineLarge
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            value = uiState.data.email,
            onValueChange = { onUiEvent(SignUpUiEvent.EmailChanged(it)) },
            singleLine = true,
            label = { Text("Email") },
            colors = appOutlinedTextFieldColors(),
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            value = uiState.data.password,
            onValueChange = { onUiEvent(SignUpUiEvent.PasswordChanged(it)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("Password") },
            colors = appOutlinedTextFieldColors(),
        )

        AppButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            state = AppButtonState(
                title = TextState("Create account"),
                enabled = !uiState.isLoading,
                onClick = { onUiEvent(SignUpUiEvent.CreateAccountClicked) },
            ),
        )

        AppButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            state = AppButtonState(
                title = TextState("Have account? Sign in"),
                enabled = !uiState.isLoading,
                onClick = { onUiEvent(SignUpUiEvent.NavigateToSignInClicked) },
            ),
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 14.dp))
        }

        uiState.error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (uiState.data.infoMessage.isNotBlank()) {
            Text(
                text = uiState.data.infoMessage,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
