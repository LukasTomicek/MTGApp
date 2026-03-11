package mtg.app.feature.map.presentation.map

import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.TextInputRow
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.feature.map.presentation.utils.TradeMapView
import mtg.app.feature.map.presentation.utils.components.MapActionButtonsRow
import mtg.app.feature.map.presentation.utils.components.MapPinsRow
import mtg.app.feature.map.presentation.utils.components.MapRadiusAndLocationSection
import mtg.app.feature.map.presentation.utils.rememberAddressSearchRequester

@Composable
fun MapScreen(
    uiState: UiState<MapScreenState>,
    onUiEvent: (MapUiEvent) -> Unit,
) {
    val requestAddressSearch = rememberAddressSearchRequester { coordinate ->
        onUiEvent(MapUiEvent.AddressResolved(coordinate))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextInputRow(
            query = uiState.data.addressQuery,
            label = "Pin by address",
            enabled = uiState.data.addressMessage.isBlank(),
            onQueryChange = { onUiEvent(MapUiEvent.AddressQueryChanged(it)) },
            onSearchClick = { requestAddressSearch(uiState.data.addressQuery) },
        )

        if (uiState.data.addressMessage.isNotBlank()) {
            Text(
                text = uiState.data.addressMessage,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.black,
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
        ) {
            TradeMapView(
                modifier = Modifier.fillMaxSize(),
                pins = uiState.data.pins,
                selectedPinId = uiState.data.selectedPinId,
                radiusMeters = uiState.data.radiusMeters,
                centerOnUserRequestId = uiState.data.centerOnUserRequestId,
                centerOnPinRequestId = uiState.data.centerOnPinRequestId,
                onUserLocationChanged = { onUiEvent(MapUiEvent.UserLocationUpdated(it)) },
                onPinAdded = { onUiEvent(MapUiEvent.PinAdded(it)) },
                onPinSelected = { onUiEvent(MapUiEvent.PinSelected(it)) },
            )
        }

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            MapActionButtonsRow(
                modifier = Modifier.padding(top = 12.dp),
                isPinSelected = uiState.data.selectedPinId != null,
                onCenterOnUserClick = { onUiEvent(MapUiEvent.CenterOnUserClicked) },
                onCenterOnPinClick = { onUiEvent(MapUiEvent.CenterOnPinClicked) },
                onRemovePinClick = { onUiEvent(MapUiEvent.RemoveSelectedPinClicked) },
            )

            if (uiState.data.pins.isNotEmpty()) {
                MapPinsRow(
                    pins = uiState.data.pins,
                    selectedPinId = uiState.data.selectedPinId,
                    onPinClick = { onUiEvent(MapUiEvent.PinSelected(it)) },
                )
            }

            MapRadiusAndLocationSection(
                radiusMeters = uiState.data.radiusMeters,
                userLocation = uiState.data.userLocation,
                selectedPinLocation = selectedPin(uiState.data),
                onRadiusChanged = { onUiEvent(MapUiEvent.RadiusChanged(it)) },
            )
        }
    }
}

private fun selectedPin(state: MapScreenState): MapCoordinate? {
    val selected = state.selectedPinId ?: return null
    return state.pins.firstOrNull { it.id == selected }?.coordinate
}
