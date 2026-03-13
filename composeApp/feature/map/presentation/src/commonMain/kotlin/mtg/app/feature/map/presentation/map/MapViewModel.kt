package mtg.app.feature.map.presentation.map

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.TradeService
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.collect

class MapViewModel(
    private val authService: AuthDomainService,
    private val tradeService: TradeService,
) : BaseViewModel<MapScreenState, MapUiEvent, MapDirection>(
    initialState = MapScreenState(),
) {
    private var nextPinNumber: Int = 1
    private var currentUid: String? = null
    private var currentEmail: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentUid = user?.uid
                currentEmail = user?.email
                currentIdToken = user?.idToken
                if (user == null) {
                    updateState { it.copy(pins = emptyList(), selectedPinId = null) }
                } else {
                    loadPersistedPins(uid = user.uid, idToken = user.idToken)
                }
            }
        }
    }

    override fun onUiEvent(event: MapUiEvent) {
        when (event) {
            is MapUiEvent.UserLocationUpdated -> {
                updateState { state ->
                    if (state.userLocation == event.coordinate) state
                    else state.copy(userLocation = event.coordinate)
                }
            }

            is MapUiEvent.PinAdded -> {
                val before = state.value.data
                updateState { state ->
                    val pin = MapPin(
                        id = "pin-${nextPinNumber++}",
                        coordinate = event.coordinate,
                    )
                    state.copy(
                        pins = state.pins + pin,
                        selectedPinId = pin.id,
                    )
                }
                syncIfChanged(before = before)
            }

            is MapUiEvent.PinSelected -> {
                updateState { state ->
                    if (state.pins.any { it.id == event.pinId }) {
                        state.copy(selectedPinId = event.pinId)
                    } else {
                        state
                    }
                }
            }

            is MapUiEvent.RadiusChanged -> {
                val before = state.value.data
                updateState { it.copy(radiusMeters = event.meters.coerceIn(100f, 50_000f)) }
                syncIfChanged(before = before)
            }

            is MapUiEvent.AddressQueryChanged -> {
                updateState { it.copy(addressQuery = event.value, addressMessage = "") }
            }

            is MapUiEvent.AddressResolved -> {
                if (event.coordinate == null) {
                    updateState { it.copy(addressMessage = "Address not found") }
                    return
                }

                val before = state.value.data
                updateState { state ->
                    val pin = MapPin(
                        id = "pin-${nextPinNumber++}",
                        coordinate = event.coordinate,
                    )
                    state.copy(
                        pins = state.pins + pin,
                        selectedPinId = pin.id,
                        centerOnPinRequestId = state.centerOnPinRequestId + 1,
                        addressMessage = "Location added as pin",
                    )
                }
                syncIfChanged(before = before)
            }

            MapUiEvent.CenterOnUserClicked -> {
                updateState { it.copy(centerOnUserRequestId = it.centerOnUserRequestId + 1) }
            }

            MapUiEvent.CenterOnPinClicked -> {
                updateState { state ->
                    if (state.selectedPinId == null) state
                    else state.copy(centerOnPinRequestId = state.centerOnPinRequestId + 1)
                }
            }

            MapUiEvent.RemoveSelectedPinClicked -> {
                val before = state.value.data
                updateState { state ->
                    val selectedId = state.selectedPinId ?: return@updateState state
                    val updatedPins = state.pins.filterNot { it.id == selectedId }
                    state.copy(
                        pins = updatedPins,
                        selectedPinId = updatedPins.lastOrNull()?.id,
                    )
                }
                syncIfChanged(before = before)
            }
        }
    }

    private fun syncIfChanged(before: MapScreenState) {
        val after = state.value.data
        if (before.pins != after.pins || before.radiusMeters != after.radiusMeters) {
            persistPins()
        }
    }

    private fun persistPins() {
        val uid = currentUid ?: return
        val email = currentEmail
        val idToken = currentIdToken ?: return
        val state = state.value.data
        val pins = state.pins.map { pin ->
            StoredMapPin(
                pinId = pin.id,
                latitude = pin.coordinate.latitude,
                longitude = pin.coordinate.longitude,
                radiusMeters = state.radiusMeters,
            )
        }

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { throwable ->
                setError(throwable.message ?: "Failed to sync map pins")
            },
            action = {
                tradeService.replaceMapPins(
                    context = AuthContext(uid = uid, idToken = idToken),
                    pins = pins,
                    actorEmail = email,
                    triggerRematch = true,
                )
            },
        )
    }

    private fun loadPersistedPins(uid: String, idToken: String) {
        domainCall(
            action = {
                tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to load map pins")
            },
        ) { persisted ->
                val pins = persisted.map {
                    MapPin(
                        id = it.pinId,
                        coordinate = MapCoordinate(latitude = it.latitude, longitude = it.longitude),
                    )
                }
                nextPinNumber = nextPinNumber(pins)
                updateState { state ->
                    state.copy(
                        pins = pins,
                        selectedPinId = pins.firstOrNull()?.id,
                        radiusMeters = persisted.firstOrNull()?.radiusMeters ?: state.radiusMeters,
                    )
                }
                tradeService.replaceMapPins(
                    context = AuthContext(uid = uid, idToken = idToken),
                    pins = persisted,
                    actorEmail = currentEmail,
                    triggerRematch = false,
                )
        }
    }
}

private fun nextPinNumber(pins: List<MapPin>): Int {
    val maxId = pins.mapNotNull { pin ->
        pin.id.removePrefix("pin-").toIntOrNull()
    }.maxOrNull() ?: 0
    return maxId + 1
}
