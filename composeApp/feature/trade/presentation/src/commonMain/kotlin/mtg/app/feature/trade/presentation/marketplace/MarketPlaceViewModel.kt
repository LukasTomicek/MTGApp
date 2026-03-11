package mtg.app.feature.trade.presentation.marketplace

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.trade.domain.EnsureMarketPlaceChatUseCase
import mtg.app.feature.trade.domain.LoadMapPinsUseCase
import mtg.app.feature.trade.domain.LoadMarketPlaceSellersUseCase
import mtg.app.feature.trade.domain.LoadRecentMarketPlaceCardsUseCase
import mtg.app.feature.trade.domain.ReplaceMapPinsUseCase
import mtg.app.feature.trade.domain.SearchMarketPlaceCardsUseCase
import mtg.app.feature.trade.domain.StoredMapPin
import kotlinx.coroutines.flow.collect

class MarketPlaceViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val searchMarketPlaceCards: SearchMarketPlaceCardsUseCase,
    private val loadRecentMarketPlaceCards: LoadRecentMarketPlaceCardsUseCase,
    private val loadMarketPlaceSellers: LoadMarketPlaceSellersUseCase,
    private val ensureMarketPlaceChat: EnsureMarketPlaceChatUseCase,
    private val loadMapPins: LoadMapPinsUseCase,
    private val replaceMapPins: ReplaceMapPinsUseCase,
) : BaseViewModel<MarketPlaceScreenState, MarketPlaceUiEvent, MarketPlaceDirection>(
    initialState = MarketPlaceScreenState(),
) {
    private var currentUid: String? = null
    private var currentEmail: String? = null
    private var currentIdToken: String? = null
    private var hasMapPins: Boolean? = null

    init {
        launch {
            observeAuthState().collect { user ->
                currentUid = user?.uid
                currentEmail = user?.email
                currentIdToken = user?.idToken
                if (user == null) {
                    hasMapPins = null
                    updateState {
                        it.copy(
                            searchResults = emptyList(),
                            recentCards = emptyList(),
                            sellersForSelectedCard = emptyList(),
                            selectedSellerUid = null,
                            showSellersDialog = false,
                            showPinRecommendationDialog = false,
                        )
                    }
                } else {
                    loadInitialData()
                    refreshMapPinsPresence(uid = user.uid, idToken = user.idToken)
                }
            }
        }
    }

    override fun onUiEvent(event: MarketPlaceUiEvent) {
        when (event) {
            is MarketPlaceUiEvent.SearchChanged -> {
                updateState { it.copy(searchQuery = event.value) }
            }

            MarketPlaceUiEvent.SearchSubmitted -> {
                loadSearchResults()
            }

            MarketPlaceUiEvent.ScreenOpened -> {
                loadInitialData()
                if (hasMapPins == false) {
                    updateState { it.copy(showPinRecommendationDialog = true) }
                } else {
                    ensureMapPinsLoadedAndPromptIfMissing()
                }
            }

            is MarketPlaceUiEvent.MarketCardClicked -> {
                openSellerDialog(
                    cardId = event.cardId,
                    cardName = event.cardName,
                )
            }

            is MarketPlaceUiEvent.SellerSelected -> {
                updateState { it.copy(selectedSellerUid = event.sellerUid) }
            }

            MarketPlaceUiEvent.MessageSellerClicked -> {
                openOrCreateChatForSelectedSeller()
            }

            MarketPlaceUiEvent.ViewSellerProfileClicked -> {
                openSelectedSellerProfile()
            }

            MarketPlaceUiEvent.SellerDialogDismissed -> {
                updateState {
                    it.copy(
                        showSellersDialog = false,
                        selectedCardId = null,
                        selectedCardName = "",
                        sellersForSelectedCard = emptyList(),
                        selectedSellerUid = null,
                    )
                }
            }

            MarketPlaceUiEvent.DismissPinRecommendationClicked -> {
                updateState { it.copy(showPinRecommendationDialog = false) }
            }

            is MarketPlaceUiEvent.CurrentLocationResolved -> {
                savePinFromCurrentLocation(event.coordinate)
            }
        }
    }

    private fun loadInitialData() {
        loadSearchResults()
        loadRecent()
    }

    private fun loadSearchResults() {
        val idToken = currentIdToken ?: return
        val viewerUid = currentUid ?: return

        launch {
            setLoading(true)
            setError(null)

            try {
                val query = state.value.data.searchQuery
                val cards = searchMarketPlaceCards(
                    idToken = idToken,
                    viewerUid = viewerUid,
                    query = query,
                )
                updateState { it.copy(searchResults = cards) }
            } catch (e: Throwable) {
                setError(e.message ?: "Failed to load marketplace cards")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun loadRecent() {
        val idToken = currentIdToken ?: return
        val viewerUid = currentUid ?: return

        launch {
            runCatching {
                loadRecentMarketPlaceCards(
                    idToken = idToken,
                    viewerUid = viewerUid,
                    limit = 20,
                )
            }.onSuccess { cards ->
                updateState { it.copy(recentCards = cards) }
            }.onFailure {
                setError(it.message ?: "Failed to load recent cards")
            }
        }
    }

    private fun openSellerDialog(cardId: String, cardName: String) {
        val idToken = currentIdToken ?: return
        val ownUid = currentUid ?: return

        launch {
            setLoading(true)
            runCatching {
                loadMarketPlaceSellers(
                    idToken = idToken,
                    viewerUid = ownUid,
                    cardId = cardId,
                    cardName = cardName,
                )
            }.onSuccess { sellers ->
                val filtered = sellers.filter { it.uid != ownUid }
                updateState {
                    it.copy(
                        selectedCardId = cardId,
                        selectedCardName = cardName,
                        sellersForSelectedCard = filtered,
                        selectedSellerUid = filtered.firstOrNull()?.uid,
                        showSellersDialog = true,
                    )
                }
            }.onFailure {
                setError(it.message ?: "Failed to load sellers")
            }
            setLoading(false)
        }
    }

    private fun openOrCreateChatForSelectedSeller() {
        val idToken = currentIdToken ?: return
        val buyerUid = currentUid ?: return
        val buyerEmail = currentEmail?.trim().orEmpty().ifBlank { buyerUid }
        val stateData = state.value.data
        val selectedSellerUid = stateData.selectedSellerUid ?: return
        val seller = stateData.sellersForSelectedCard.firstOrNull { it.uid == selectedSellerUid } ?: return
        if (seller.uid == buyerUid) {
            setError("You can't message yourself")
            return
        }
        val cardId = stateData.selectedCardId.orEmpty().ifBlank { stateData.selectedCardName }
        val cardName = stateData.selectedCardName.ifBlank { "Unknown card" }

        launch {
            setLoading(true)
            runCatching {
                ensureMarketPlaceChat(
                    idToken = idToken,
                    buyerUid = buyerUid,
                    buyerEmail = buyerEmail,
                    sellerUid = seller.uid,
                    sellerEmail = seller.displayName,
                    cardId = cardId,
                    cardName = cardName,
                )
            }.onSuccess { chatId ->
                updateState {
                    it.copy(
                        showSellersDialog = false,
                        selectedCardId = null,
                        selectedCardName = "",
                        sellersForSelectedCard = emptyList(),
                        selectedSellerUid = null,
                    )
                }
                navigate(MarketPlaceDirection.NavigateToChat(chatId))
            }.onFailure {
                setError(it.message ?: "Failed to open chat")
            }
            setLoading(false)
        }
    }

    private fun openSelectedSellerProfile() {
        val selectedUid = state.value.data.selectedSellerUid ?: return
        updateState {
            it.copy(
                showSellersDialog = false,
                selectedCardId = null,
                selectedCardName = "",
                sellersForSelectedCard = emptyList(),
                selectedSellerUid = null,
            )
        }
        navigate(MarketPlaceDirection.NavigateToPublicProfile(selectedUid))
    }

    private fun refreshMapPinsPresence(uid: String, idToken: String) {
        launch {
            runCatching {
                loadMapPins(uid = uid, idToken = idToken)
            }.onSuccess { pins ->
                hasMapPins = pins.isNotEmpty()
                updateState {
                    it.copy(showPinRecommendationDialog = pins.isEmpty())
                }
            }.onFailure {
                hasMapPins = null
                updateState {
                    it.copy(showPinRecommendationDialog = true)
                }
            }
        }
    }

    private fun ensureMapPinsLoadedAndPromptIfMissing() {
        if (hasMapPins != null) return
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        launch {
            runCatching {
                loadMapPins(uid = uid, idToken = idToken)
            }.onSuccess { pins ->
                hasMapPins = pins.isNotEmpty()
                if (pins.isEmpty()) {
                    updateState { it.copy(showPinRecommendationDialog = true) }
                }
            }.onFailure {
                hasMapPins = null
                updateState { it.copy(showPinRecommendationDialog = true) }
            }
        }
    }

    private fun savePinFromCurrentLocation(coordinate: mtg.app.feature.map.presentation.map.MapCoordinate?) {
        val uid = currentUid
        val idToken = currentIdToken

        if (uid == null || idToken == null) {
            updateState {
                it.copy(
                    showPinRecommendationDialog = false,
                    selectedCardName = "",
                )
            }
            return
        }

        if (coordinate == null) {
            updateState { it.copy(showPinRecommendationDialog = false) }
            return
        }

        launch {
            runCatching {
                val existing = loadMapPins(uid = uid, idToken = idToken)
                val nextPin = StoredMapPin(
                    pinId = "pin-${nextMapPinNumber(existing) + 1}",
                    latitude = coordinate.latitude,
                    longitude = coordinate.longitude,
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
                hasMapPins = true
                updateState { it.copy(showPinRecommendationDialog = false) }
                loadInitialData()
            }.onFailure {
                updateState { it.copy(showPinRecommendationDialog = false) }
            }
        }
    }
}

private fun nextMapPinNumber(pins: List<StoredMapPin>): Int {
    return pins.maxOfOrNull { pin ->
        pin.pinId.removePrefix("pin-").toIntOrNull() ?: 0
    } ?: 0
}
