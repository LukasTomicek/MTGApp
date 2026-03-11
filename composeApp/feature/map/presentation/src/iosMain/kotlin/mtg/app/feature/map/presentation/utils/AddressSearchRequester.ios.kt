package mtg.app.feature.map.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import mtg.app.feature.map.presentation.map.MapCoordinate

@Composable
actual fun rememberAddressSearchRequester(
    onResult: (MapCoordinate?) -> Unit,
): (String) -> Unit {
    return remember(onResult) {
        { _ -> onResult(null) }
    }
}
