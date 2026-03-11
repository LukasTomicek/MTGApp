package mtg.app.feature.map.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mtg.app.feature.map.presentation.map.MapCoordinate
import mtg.app.feature.map.presentation.map.MapPin

@Composable
actual fun TradeMapView(
    modifier: Modifier,
    pins: List<MapPin>,
    selectedPinId: String?,
    radiusMeters: Float,
    centerOnUserRequestId: Int,
    centerOnPinRequestId: Int,
    onUserLocationChanged: (MapCoordinate) -> Unit,
    onPinAdded: (MapCoordinate) -> Unit,
    onPinSelected: (String) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Map is available on Android build.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
