package mtg.app.feature.trade.presentation.marketplace

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.trade.domain.MarketPlaceOfferType
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.TradeService
import mtg.app.feature.trade.domain.obj.EnsureMarketPlaceChatRequest
import mtg.app.feature.trade.domain.obj.MarketPlaceCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceRecentCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceSellersQuery
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.collect

class MarketPlaceViewModel(
    private val authService: AuthDomainService,
    private val tradeService: TradeService,
) : BaseViewModel<MarketPlaceScreenState, MarketPlaceUiEvent, MarketPlaceDirection>(
    initialState = MarketPlaceScreenState(),
) {
    private var currentUid: String? = null
    private var currentEmail: String? = null
    private var currentIdToken: String? = null
    private var hasMapPins: Boolean? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentUid = user?.uid
                currentEmail = user?.email
                currentIdToken = user?.idToken
                if (user == null) {
                    hasMapPins = null
                    updateState {
                        it.copy(
                            sellSearchResults = emptyList(),
                            buySearchResults = emptyList(),
                            recentSellCards = emptyList(),
                            recentBuyCards = emptyList(),
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

            MarketPlaceUiEvent.ToggleDisplayModeClicked -> {
                updateState {
                    it.copy(
                        displayMode = when (it.displayMode) {
                            MarketPlaceDisplayMode.SELL -> MarketPlaceDisplayMode.BUY
                            MarketPlaceDisplayMode.BUY -> MarketPlaceDisplayMode.SELL
                        }
                    )
                }
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
                val cards = tradeService.searchMarketPlaceCards(
                    context = AuthContext(uid = viewerUid, idToken = idToken),
                    query = MarketPlaceCardsQuery(
                        query = query,
                        offerType = MarketPlaceOfferType.SELL,
                    ),
                )
                val buyCards = tradeService.searchMarketPlaceCards(
                    context = AuthContext(uid = viewerUid, idToken = idToken),
                    query = MarketPlaceCardsQuery(
                        query = query,
                        offerType = MarketPlaceOfferType.BUY,
                    ),
                )
                updateState {
                    it.copy(
                        sellSearchResults = cards,
                        buySearchResults = buyCards,
                    )
                }
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
                tradeService.loadRecentMarketPlaceCards(
                    context = AuthContext(uid = viewerUid, idToken = idToken),
                    query = MarketPlaceRecentCardsQuery(
                        limit = 20,
                        offerType = MarketPlaceOfferType.SELL,
                    ),
                )
            }.onSuccess { cards ->
                updateState { it.copy(recentSellCards = cards) }
            }.onFailure {
                setError(it.message ?: "Failed to load recent cards")
            }
        }

        launch {
            runCatching {
                tradeService.loadRecentMarketPlaceCards(
                    context = AuthContext(uid = viewerUid, idToken = idToken),
                    query = MarketPlaceRecentCardsQuery(
                        limit = 20,
                        offerType = MarketPlaceOfferType.BUY,
                    ),
                )
            }.onSuccess { cards ->
                updateState { it.copy(recentBuyCards = cards) }
            }.onFailure {
                setError(it.message ?: "Failed to load recent buy requests")
            }
        }
    }

    private fun openSellerDialog(cardId: String, cardName: String) {
        val idToken = currentIdToken ?: return
        val ownUid = currentUid ?: return

        launch {
            setLoading(true)
            runCatching {
                tradeService.loadMarketPlaceSellers(
                    context = AuthContext(uid = ownUid, idToken = idToken),
                    query = MarketPlaceSellersQuery(
                        cardId = cardId,
                        cardName = cardName,
                    ),
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
                tradeService.ensureMarketPlaceChat(
                    context = AuthContext(uid = buyerUid, idToken = idToken),
                    request = EnsureMarketPlaceChatRequest(
                        buyerUid = buyerUid,
                        buyerEmail = buyerEmail,
                        sellerUid = seller.uid,
                        sellerEmail = seller.displayName,
                        cardId = cardId,
                        cardName = cardName,
                    ),
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
                tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
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
                tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
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
                val existing = tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
                val nextPin = StoredMapPin(
                    pinId = "pin-${nextMapPinNumber(existing) + 1}",
                    latitude = coordinate.latitude,
                    longitude = coordinate.longitude,
                    radiusMeters = if (existing.isEmpty()) 1_000f else existing.first().radiusMeters,
                )
                tradeService.replaceMapPins(
                    context = AuthContext(uid = uid, idToken = idToken),
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
