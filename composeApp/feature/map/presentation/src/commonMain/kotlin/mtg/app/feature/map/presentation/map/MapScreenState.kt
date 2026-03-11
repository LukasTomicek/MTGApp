package mtg.app.feature.map.presentation.map

data class MapScreenState(
    val title: String = "Map",

    // Address search
    val addressQuery: String = "",
    val addressMessage: String = "",

    // Pins
    val pins: List<MapPin> = emptyList(),
    val selectedPinId: String? = null,

    // Radius and user location
    val radiusMeters: Float = 1_000f,
    val userLocation: MapCoordinate? = null,

    // One-shot map actions
    val centerOnUserRequestId: Int = 0,
    val centerOnPinRequestId: Int = 0,
)

data class MapCoordinate(
    val latitude: Double,
    val longitude: Double,
)

data class MapPin(
    val id: String,
    val coordinate: MapCoordinate,
)
