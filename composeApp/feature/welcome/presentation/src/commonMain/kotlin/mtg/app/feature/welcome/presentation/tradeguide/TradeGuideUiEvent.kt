package mtg.app.feature.welcome.presentation.tradeguide

import mtg.app.core.presentation.Event

sealed interface TradeGuideUiEvent : Event {
    data object ContinueClicked : TradeGuideUiEvent
    data object BackClicked : TradeGuideUiEvent
}
