package mtg.app.feature.map.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import mtg.app.feature.map.presentation.map.MapCoordinate

@Composable
actual fun rememberCurrentLocationRequester(
    onLocationResult: (MapCoordinate?) -> Unit,
): () -> Unit {
    return remember(onLocationResult) {
        { onLocationResult(null) }
    }
}
