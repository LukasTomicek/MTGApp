package mtg.app.feature.welcome.presentation.mapguide

import mtg.app.core.presentation.Direction

sealed interface MapGuideDirection : Direction {
    data object NavigateToTradeGuide : MapGuideDirection
    data object NavigateBack : MapGuideDirection
}
