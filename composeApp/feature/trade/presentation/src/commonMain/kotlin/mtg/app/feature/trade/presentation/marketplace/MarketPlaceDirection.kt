package mtg.app.feature.trade.presentation.marketplace

import mtg.app.core.presentation.Direction

sealed interface MarketPlaceDirection : Direction {
    data object None : MarketPlaceDirection
    data class NavigateToChat(val chatId: String) : MarketPlaceDirection
    data class NavigateToPublicProfile(val uid: String) : MarketPlaceDirection
}
