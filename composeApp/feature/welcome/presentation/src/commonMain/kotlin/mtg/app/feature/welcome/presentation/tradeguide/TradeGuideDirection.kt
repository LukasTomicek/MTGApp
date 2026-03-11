package mtg.app.feature.welcome.presentation.tradeguide

import mtg.app.core.presentation.Direction

sealed interface TradeGuideDirection : Direction {
    data object NavigateToSetupProfile : TradeGuideDirection
    data object NavigateBack : TradeGuideDirection
}
