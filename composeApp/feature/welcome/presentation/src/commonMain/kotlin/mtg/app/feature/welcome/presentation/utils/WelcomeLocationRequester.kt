package mtg.app.feature.welcome.presentation

import androidx.compose.runtime.Composable

data class WelcomeLocation(
    val latitude: Double,
    val longitude: Double,
)

@Composable
expect fun rememberCurrentLocationRequester(
    onLocationResult: (WelcomeLocation?) -> Unit,
): () -> Unit
