package mtg.app.feature.trade.presentation.marketplace

import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller

data class MarketPlaceScreenState(
    // Search
    val searchQuery: String = "",
    val searchResults: List<MarketPlaceCard> = emptyList(),
    val recentCards: List<MarketPlaceCard> = emptyList(),

    // Selected card context
    val selectedCardId: String? = null,
    val selectedCardName: String = "",

    // Sellers modal data
    val sellersForSelectedCard: List<MarketPlaceSeller> = emptyList(),
    val selectedSellerUid: String? = null,
    val showSellersDialog: Boolean = false,
    val showPinRecommendationDialog: Boolean = false,
)
