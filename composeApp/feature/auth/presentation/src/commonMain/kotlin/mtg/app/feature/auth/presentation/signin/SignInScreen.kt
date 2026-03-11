package mtg.app.feature.auth.presentation.signin

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.theme.AppTheme

@Composable
fun SignInScreen(
    uiState: UiState<SignInScreenState>,
    onUiEvent: (SignInUiEvent) -> Unit,
) {
    val requestGoogleIdToken = rememberGoogleIdTokenRequester(
        onTokenReceived = { onUiEvent(SignInUiEvent.GoogleIdTokenReceived(it)) },
        onError = { onUiEvent(SignInUiEvent.GoogleSignInFailed(it)) },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sign in",
            style = MaterialTheme.typography.headlineLarge
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            value = uiState.data.email,
            onValueChange = { onUiEvent(SignInUiEvent.EmailChanged(it)) },
            singleLine = true,
            label = { Text("Email") },
            colors = appOutlinedTextFieldColors(),
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            value = uiState.data.password,
            onValueChange = { onUiEvent(SignInUiEvent.PasswordChanged(it)) },
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
                title = TextState("Sign in"),
                enabled = !uiState.isLoading,
                onClick = { onUiEvent(SignInUiEvent.SignInClicked) },
            ),
        )

        AppButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            state = AppButtonState(
                title = TextState("Continue with Google"),
                enabled = !uiState.isLoading,
                onClick = {
                    onUiEvent(SignInUiEvent.GoogleSignInClicked)
                    requestGoogleIdToken()
                },
            ),
        )

        AppButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            state = AppButtonState(
                title = TextState("Create account"),
                enabled = !uiState.isLoading,
                onClick = { onUiEvent(SignInUiEvent.NavigateToSignUpClicked) },
            ),
        )

        AppButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            state = AppButtonState(
                title = TextState("Forgot password"),
                enabled = !uiState.isLoading,
                onClick = { onUiEvent(SignInUiEvent.NavigateToForgotPasswordClicked) },
            ),
        )

        uiState.error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 10.dp),
                color = AppTheme.colors.error,
                style = AppTheme.typography.bodySmall,
            )
        }

        if (uiState.data.infoMessage.isNotBlank()) {
            Text(
                text = uiState.data.infoMessage,
                modifier = Modifier.padding(top = 8.dp),
                color = AppTheme.colors.black,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}
