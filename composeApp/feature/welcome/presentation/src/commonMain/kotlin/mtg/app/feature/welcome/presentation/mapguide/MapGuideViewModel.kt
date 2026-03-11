package mtg.app.feature.welcome.presentation.mapguide

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.trade.domain.LoadMapPinsUseCase
import mtg.app.feature.trade.domain.ReplaceMapPinsUseCase
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.welcome.presentation.WelcomeLocation
import kotlinx.coroutines.flow.collectLatest

class MapGuideViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val loadMapPins: LoadMapPinsUseCase,
    private val replaceMapPins: ReplaceMapPinsUseCase,
) : BaseViewModel<MapGuideScreenState, MapGuideUiEvent, MapGuideDirection>(
    initialState = MapGuideScreenState(),
) {
    private var currentUid: String? = null
    private var currentEmail: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            observeAuthState().collectLatest { user ->
                currentUid = user?.uid
                currentEmail = user?.email
                currentIdToken = user?.idToken
            }
        }
    }

    override fun onUiEvent(event: MapGuideUiEvent) {
        when (event) {
            MapGuideUiEvent.BackClicked -> navigate(MapGuideDirection.NavigateBack)
            MapGuideUiEvent.ContinueClicked -> navigate(MapGuideDirection.NavigateToTradeGuide)
            MapGuideUiEvent.SetCurrentLocationAsDefaultPinClicked -> {
                updateState { it.copy(infoMessage = "Requesting current location...") }
            }
            is MapGuideUiEvent.CurrentLocationResolved -> onCurrentLocationResolved(event.location)
        }
    }

    private fun onCurrentLocationResolved(location: WelcomeLocation?) {
        val uid = currentUid
        val idToken = currentIdToken
        if (uid == null || idToken == null) {
            updateState { it.copy(infoMessage = "Sign in required before storing map defaults") }
            return
        }
        if (location == null) {
            updateState { it.copy(infoMessage = "Unable to get current location. Check permissions.") }
            return
        }

        launch {
            setLoading(true)
            setError(null)

            runCatching {
                val existing = loadMapPins(uid = uid, idToken = idToken)
                val nextPin = StoredMapPin(
                    pinId = "pin-${nextPinNumber(existing) + 1}",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radiusMeters = if (existing.isEmpty()) 1_000f else existing.first().radiusMeters,
                )
                replaceMapPins(
                    uid = uid,
                    idToken = idToken,
                    pins = existing + nextPin,
                    actorEmail = currentEmail,
                    triggerRematch = true,
                )
            }.onSuccess {
                updateState { it.copy(infoMessage = "Current location saved as default map pin") }
            }.onFailure {
                setError(it.message ?: "Failed to save map pin")
            }

            setLoading(false)
        }
    }
}

private fun nextPinNumber(pins: List<StoredMapPin>): Int {
    return pins.mapNotNull { pin ->
        pin.pinId.removePrefix("pin-").toIntOrNull()
    }.maxOrNull() ?: 0
}
