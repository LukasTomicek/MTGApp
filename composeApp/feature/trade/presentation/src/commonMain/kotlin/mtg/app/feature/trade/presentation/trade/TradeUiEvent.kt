package mtg.app.feature.trade.presentation.trade

import mtg.app.core.presentation.Event

sealed interface TradeUiEvent : Event {
    data object CollectionClicked : TradeUiEvent
    data object MarketPlaceClicked : TradeUiEvent
    data object BuyListClicked : TradeUiEvent
    data object SellListClicked : TradeUiEvent
}
