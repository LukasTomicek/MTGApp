package mtg.app.feature.trade.presentation.trade

import mtg.app.core.presentation.BaseViewModel

class TradeViewModel : BaseViewModel<TradeScreenState, TradeUiEvent, TradeDirection>(
    initialState = TradeScreenState(),
) {

    override fun onUiEvent(event: TradeUiEvent) {
        when (event) {
            TradeUiEvent.CollectionClicked -> navigate(TradeDirection.NavigateToCollection)
            TradeUiEvent.MarketPlaceClicked -> navigate(TradeDirection.NavigateToMarketPlace)
            TradeUiEvent.BuyListClicked -> navigate(TradeDirection.NavigateToBuyList)
            TradeUiEvent.SellListClicked -> navigate(TradeDirection.NavigateToSellList)
        }
    }
}
