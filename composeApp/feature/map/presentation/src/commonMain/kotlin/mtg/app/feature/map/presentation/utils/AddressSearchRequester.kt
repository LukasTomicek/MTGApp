package mtg.app.feature.map.presentation.utils

import androidx.compose.runtime.Composable
import mtg.app.feature.map.presentation.map.MapCoordinate

@Composable
expect fun rememberAddressSearchRequester(
    onResult: (MapCoordinate?) -> Unit,
): (String) -> Unit
