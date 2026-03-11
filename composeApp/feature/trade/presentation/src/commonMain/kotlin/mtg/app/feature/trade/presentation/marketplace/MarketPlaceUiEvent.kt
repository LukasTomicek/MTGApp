package mtg.app.feature.trade.presentation.marketplace

import mtg.app.core.presentation.Event
import mtg.app.feature.map.presentation.map.MapCoordinate

sealed interface MarketPlaceUiEvent : Event {
    data class SearchChanged(val value: String) : MarketPlaceUiEvent
    data object SearchSubmitted : MarketPlaceUiEvent
    data object ScreenOpened : MarketPlaceUiEvent
    data class MarketCardClicked(
        val cardId: String,
        val cardName: String,
    ) : MarketPlaceUiEvent
    data class SellerSelected(val sellerUid: String) : MarketPlaceUiEvent
    data object MessageSellerClicked : MarketPlaceUiEvent
    data object ViewSellerProfileClicked : MarketPlaceUiEvent
    data object SellerDialogDismissed : MarketPlaceUiEvent
    data object DismissPinRecommendationClicked : MarketPlaceUiEvent
    data class CurrentLocationResolved(val coordinate: MapCoordinate?) : MarketPlaceUiEvent
}
