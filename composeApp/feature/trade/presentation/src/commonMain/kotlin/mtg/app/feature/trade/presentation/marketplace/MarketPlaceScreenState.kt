package mtg.app.feature.trade.presentation.marketplace

import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller

enum class MarketPlaceDisplayMode {
    SELL,
    BUY,
}

data class MarketPlaceScreenState(
    // Search
    val searchQuery: String = "",
    val sellSearchResults: List<MarketPlaceCard> = emptyList(),
    val buySearchResults: List<MarketPlaceCard> = emptyList(),
    val recentSellCards: List<MarketPlaceCard> = emptyList(),
    val recentBuyCards: List<MarketPlaceCard> = emptyList(),
    val displayMode: MarketPlaceDisplayMode = MarketPlaceDisplayMode.SELL,

    // Selected card context
    val selectedCardId: String? = null,
    val selectedCardName: String = "",

    // Sellers modal data
    val sellersForSelectedCard: List<MarketPlaceSeller> = emptyList(),
    val selectedSellerUid: String? = null,
    val showSellersDialog: Boolean = false,
    val showPinRecommendationDialog: Boolean = false,
)
