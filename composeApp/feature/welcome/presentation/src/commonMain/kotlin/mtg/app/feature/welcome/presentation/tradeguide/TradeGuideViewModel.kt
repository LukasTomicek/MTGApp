package mtg.app.feature.welcome.presentation.tradeguide

import mtg.app.core.presentation.BaseViewModel

class TradeGuideViewModel(
) : BaseViewModel<TradeGuideScreenState, TradeGuideUiEvent, TradeGuideDirection>(
    initialState = TradeGuideScreenState(),
) {
    override fun onUiEvent(event: TradeGuideUiEvent) {
        when (event) {
            TradeGuideUiEvent.BackClicked -> navigate(TradeGuideDirection.NavigateBack)
            TradeGuideUiEvent.ContinueClicked -> navigate(TradeGuideDirection.NavigateToSetupProfile)
        }
    }
}
