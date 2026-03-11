package mtg.app.feature.map.presentation.map

import mtg.app.core.presentation.Event

sealed interface MapUiEvent : Event {
    data class UserLocationUpdated(val coordinate: MapCoordinate) : MapUiEvent
    data class PinAdded(val coordinate: MapCoordinate) : MapUiEvent
    data class PinSelected(val pinId: String) : MapUiEvent
    data class RadiusChanged(val meters: Float) : MapUiEvent
    data class AddressQueryChanged(val value: String) : MapUiEvent
    data class AddressResolved(val coordinate: MapCoordinate?) : MapUiEvent
    data object CenterOnUserClicked : MapUiEvent
    data object CenterOnPinClicked : MapUiEvent
    data object RemoveSelectedPinClicked : MapUiEvent
}
