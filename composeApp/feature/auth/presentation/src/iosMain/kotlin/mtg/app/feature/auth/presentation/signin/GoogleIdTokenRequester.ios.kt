package mtg.app.feature.auth.presentation.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGoogleIdTokenRequester(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    return remember {
        { onError("Google sign in is available only on Android for now") }
    }
}
