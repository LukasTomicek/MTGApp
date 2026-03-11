package mtg.app.feature.trade.presentation.trade

import mtg.app.core.presentation.Direction

sealed interface TradeDirection : Direction {
    data object NavigateToCollection : TradeDirection
    data object NavigateToMarketPlace : TradeDirection
    data object NavigateToBuyList : TradeDirection
    data object NavigateToSellList : TradeDirection
}
