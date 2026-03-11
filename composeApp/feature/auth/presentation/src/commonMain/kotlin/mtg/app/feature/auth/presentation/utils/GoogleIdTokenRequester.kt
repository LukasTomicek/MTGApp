package mtg.app.feature.auth.presentation.signin

import androidx.compose.runtime.Composable

@Composable
expect fun rememberGoogleIdTokenRequester(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
): () -> Unit
