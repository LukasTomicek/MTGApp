package mtg.app.feature.map.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mtg.app.feature.map.presentation.map.MapCoordinate
import mtg.app.feature.map.presentation.map.MapPin

@Composable
expect fun TradeMapView(
    modifier: Modifier,
    pins: List<MapPin>,
    selectedPinId: String?,
    radiusMeters: Float,
    centerOnUserRequestId: Int,
    centerOnPinRequestId: Int,
    onUserLocationChanged: (MapCoordinate) -> Unit,
    onPinAdded: (MapCoordinate) -> Unit,
    onPinSelected: (String) -> Unit,
)
