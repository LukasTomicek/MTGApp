package mtg.app.feature.welcome.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCurrentLocationRequester(
    onLocationResult: (WelcomeLocation?) -> Unit,
): () -> Unit {
    return remember(onLocationResult) {
        { onLocationResult(null) }
    }
}
